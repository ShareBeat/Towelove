package blossom.project.towelove.user.service.impl;

import blossom.project.towelove.common.constant.UserConstants;
import blossom.project.towelove.common.exception.ServiceException;
import blossom.project.towelove.framework.log.annotation.LoveLog;
import blossom.project.towelove.framework.redis.service.RedisService;
import blossom.project.towelove.user.constants.SignInConstants;
import blossom.project.towelove.user.constants.UserSignInTotalType;
import blossom.project.towelove.user.domain.UserSignInLog;
import blossom.project.towelove.user.domain.UserSignInRecord;
import blossom.project.towelove.user.mapper.UserSignInRecordMapper;
import blossom.project.towelove.user.service.UserSignLogService;
import blossom.project.towelove.user.service.UserSignRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: Towelove
 * @package: blossom.project.towelove.user.service.impl
 * @className: UserSignInRecordServiceImpl
 * @author: Link Ji
 * @description: TODO
 * @date: 2023/11/27 23:29
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSignInRecordServiceImpl extends ServiceImpl<UserSignInRecordMapper, UserSignInRecord> implements UserSignRecordService {

    private final RedisService redisService;

    private final RedisTemplate<String,Object> redisTemplate;

    @Qualifier("ioDynamicThreadPool")
    private final ThreadPoolExecutor ioThreadPool;

    private final UserSignLogService userSignLogService;
    @Transactional
    @Override
    public String singnInByUserId(Long userId) {
        log.info("用户：{} 发起签到请求",userId);
        String userSignInKey = getUserSignInKey(userId);
        log.info("用户签到对应签到表key为:{}",userSignInKey);
        //获取当天的偏移量
        long offset = Long
                .parseLong(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd")));
        if (redisService.getBit(userSignInKey,offset)) {
            throw new ServiceException("用户已经签到了");
        }
        /**
         * TODO:考虑使用分布式锁防止并发问题
         */
        if (redisService.setBit(userSignInKey,offset,true)) {
            throw new ServiceException("用户签到失败，请联系管理员");
        }
        String userSignInTotallyOnYear = getYearTotallyKey(userId);
        String userSignInTotallyOnMouth = getMonthTotallyKey(userId);
        if (Objects.nonNull(redisTemplate.opsForValue().get(userSignInTotallyOnYear))) {
            redisTemplate.opsForValue().increment(userSignInTotallyOnYear);
        }
        if (Objects.nonNull(redisTemplate.opsForValue().get(userSignInTotallyOnMouth))) {
            redisTemplate.opsForValue().increment(userSignInTotallyOnMouth);
        }
        //异步更新总签到天数和月签到天数
        ioThreadPool.submit(()->{saveToSignInLog(userId);});
        return "签到成功";
    }

    @Override
    public Long getSignInTotally(Long userId) {
        String userTotalSignInKey = getYearTotallyKey(userId);
        log.info("用户：{}查询年签到记录构建key为：{}",userTotalSignInKey,userTotalSignInKey);
        Object userSignINTotally = redisService.redisTemplate.opsForValue().get(userTotalSignInKey);
        if (Objects.nonNull(userSignINTotally)){
            return Long.parseLong(userSignINTotally.toString());
        }
        Long bitByRange = getUserSignInTotallyByType(userId,UserSignInTotalType.YEAR);
        log.info("用户：{}查询年签到记录缓存未命中，查询结果为：{}",userTotalSignInKey,bitByRange);
        redisTemplate.opsForValue().set(userTotalSignInKey,bitByRange,10, TimeUnit.HOURS);
        return bitByRange;
    }
    @Transactional
    @Override
    public Long getSignInByMouthTotally(Long userId) {
        String userTotalSignInByMouthKey = getMonthTotallyKey(userId);
        log.info("用户：{}查询月签到记录构建key为：{}",userId,userTotalSignInByMouthKey);
        Object userSignINTotally = redisService.redisTemplate.opsForValue().get(userTotalSignInByMouthKey);
        if (Objects.nonNull(userSignINTotally)){
            return Long.parseLong(userSignINTotally.toString());
        }
        String userSignInKey = getUserSignInKey(userId);
        long bitByRange = getUserSignInTotallyByType(userId,UserSignInTotalType.MONTH);
        log.info("用户：{}查询月签到记录缓存未命中，查询结果为：{}",userId,bitByRange);
        redisTemplate.opsForValue().set(userTotalSignInByMouthKey,bitByRange,10,TimeUnit.HOURS);
        //异步更新到数据库
//        ioThreadPool.submit(() -> {saveToSignInLog(userId);});
        return bitByRange;
    }

    /**
     * 记录签到日志表
     * 事务级别：Requires_New 若没有事务则新建事务
     * @param userId
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToSignInLog(Long userId){
        UserSignInLog userSignInLog = new UserSignInLog();
        userSignInLog.setUserId(userId);
        if (!userSignLogService.save(userSignInLog)) {
            throw new ServiceException("用户签到记录失败");
        }
    }

    /**
     * 根据类型获取签到数据
     * @param userId，userSignInTotalType
     * @return
     */
    public Long getUserSignInTotallyByType(Long userId, UserSignInTotalType userSignInTotalType){
        switch (userSignInTotalType){
            case YEAR -> {
                String key = getCommonSignInMonthKey(userId);
                //redis脚本
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setResultType(Long.class);
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(SignInConstants.luaPath)));
                return redisTemplate.execute(redisScript, List.of(key));
            }
            case MONTH -> {
                String userSignInKey = getUserSignInKey(userId);
                return redisService.getBitByRange(userSignInKey);
            }
            case CONTINUOUS -> {
                return 0L;
            }
        }
        return -1L;
    }

//    /**
//     * 获取本月某用户签到天数
//     * @param key: 用户签到BitMap的Key
//     * @return 当月签到总数量
//     */
//    public long getContinuousSignCountByMouth(String key){
//        //获得本月开始时候的偏移量
//        return redisService.getBitByRange(key);
//    }

    public String getUserSignInKey(Long userId){
        DateTime currentTime = DateTime.now();
        return String.format(UserConstants.USER_SIGN_IN_KEY
                ,currentTime.get(DateTimeFieldType.year())
                ,currentTime.get(DateTimeFieldType.monthOfYear())
                ,userId);
    }
    public String getYearTotallyKey(Long userId){
        DateTime currentTime = DateTime.now();
        return String.format(UserConstants.USER_TOTAL_SIGN_IN_KEY
                ,currentTime.get(DateTimeFieldType.year()), userId);
    }
    public String getMonthTotallyKey(Long userId){
        DateTime currentTime = DateTime.now();
        return String.format(UserConstants.USER_TOTAL_SIGN_IN_BY_MOUTH_KEY,currentTime.get(DateTimeFieldType.year())
                ,currentTime.get(DateTimeFieldType.monthOfYear())
                ,userId);
    }
    public String getCommonSignInMonthKey(Long userId){
        DateTime currentTime = DateTime.now();
        return String.format(UserConstants.USER_SIGN_IN_KEY
                ,currentTime.get(DateTimeFieldType.year())
                ,"*"
                ,userId);
    }
}
