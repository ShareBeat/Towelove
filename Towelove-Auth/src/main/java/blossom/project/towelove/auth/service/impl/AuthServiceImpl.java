package blossom.project.towelove.auth.service.impl;

import blossom.project.towelove.auth.service.AuthService;
import blossom.project.towelove.auth.strategy.UserRegisterStrategy;
import blossom.project.towelove.auth.strategy.UserRegisterStrategyFactory;
import blossom.project.towelove.client.serivce.RemoteCodeService;
import blossom.project.towelove.client.serivce.RemoteUserService;
import blossom.project.towelove.common.constant.UserConstants;
import blossom.project.towelove.common.domain.dto.SysUser;
import blossom.project.towelove.common.exception.ServiceException;
import blossom.project.towelove.common.request.auth.AuthLoginRequest;
import blossom.project.towelove.common.request.auth.AuthRegisterRequest;
import blossom.project.towelove.common.request.auth.AuthVerifyCodeRequest;
import blossom.project.towelove.common.response.Result;
import blossom.project.towelove.framework.redis.service.RedisService;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.towelove.common.core.constant.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RemoteUserService remoteUserService;

    private final RemoteCodeService remoteCodeService;

    @Override
    public String register(@Valid AuthRegisterRequest authLoginRequest) {
        //校验手机号以及邮箱格式，校验验证码格式是否正确
        UserRegisterStrategy userRegisterStrategy = UserRegisterStrategyFactory.userRegisterStrategy(authLoginRequest.getType());
        if (Objects.isNull(userRegisterStrategy)){
            throw new ServiceException("非法请求，注册渠道错误");
        }
        //验证码校验
        if (!userRegisterStrategy.valid(authLoginRequest)) {
            throw new ServiceException("验证码校验失败，请输入正确的验证码");
        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(authLoginRequest,sysUser);
        Result<String> result = remoteUserService.saveUser(sysUser);
        log.info("调用user远程服务获取到的接口为: {}",result);
        if (Objects.isNull(result) || result.getCode() != HttpStatus.SUCCESS){
            throw new ServiceException(result.getMsg());
        }
        StpUtil.login(result.getData());
        return StpUtil.getTokenInfo().tokenValue;
    }

    @Override
    public String login(AuthLoginRequest authLoginRequest) {
        //校验手机号以及邮箱格式，校验验证码格式是否正确
        UserRegisterStrategy userRegisterStrategy = UserRegisterStrategyFactory.userRegisterStrategy(authLoginRequest.getType());
        if (Objects.isNull(userRegisterStrategy)){
            throw new ServiceException("非法请求，注册渠道错误");
        }
        //验证码校验
        if (!userRegisterStrategy.valid(authLoginRequest)) {
            throw new ServiceException("验证码校验失败，请输入正确的验证码");
        }
        Result<String> result = remoteUserService.findUserByPhoneOrEmail(authLoginRequest);
        log.info("调用user远程服务获取到的接口为: {}",result);
        if (Objects.isNull(result) || result.getCode() != (HttpStatus.SUCCESS)){
            //用户不存在
            throw new ServiceException("用户不存在，请重新注册");
        }
        StpUtil.login(result.getData());
        return StpUtil.getTokenInfo().tokenValue;
    }

    @Override
    public String sendVerifyCode(AuthVerifyCodeRequest authVerifyCodeRequest) {
        if (StrUtil.isNotBlank(authVerifyCodeRequest.getPhone())){
            checkPhoneByRegex(authVerifyCodeRequest.getPhone());
            //TODO 调用远程验证码接口
            remoteCodeService.sendValidateCodeByPhone(authVerifyCodeRequest.getPhone());
        }
        else if (StrUtil.isNotBlank(authVerifyCodeRequest.getEmail())){
            checkEmailByRegex(authVerifyCodeRequest.getEmail());
            //TODO 调用远程验证码接口
            remoteCodeService.sendValidateCodeByEmail(authVerifyCodeRequest.getEmail());
        }else {
            throw new ServiceException("发送验证码失败，邮箱或手机号为空");
        }
        return "发送验证码成功";
    }

//    public void check (AuthLoginRequest authLoginRequest){
//        String type = authLoginRequest.getType();
//        if (PHONE.type.equals(type)){
//
//        }
//        //校验验证码是否正确
//        //TODO：等待验证码
//        String phone = authLoginRequest.getPhoneNumber();
//        String email = authLoginRequest.getEmail();
//        String code = authLoginRequest.getVerifyCode();
//        if (StrUtil.isNotBlank(phone)){
//            checkPhoneByRegex(phone);
//            checkVerifyCode(phone,code);
//            return;
//        }
//        if (StrUtil.isNotBlank(email)){
//            checkEmailByRegex(email);
//            checkVerifyCode(email,code);
//            return;
//        }
//        throw new ServiceException("验证码校验失败,手机号或邮箱为空");
//    }
//    public boolean checkVerifyCode(String key,String code){
//        //TODO 等待验证码接口
//        String codeFromSystem = (String) redisService.redisTemplate.opsForValue().get(RedisKeyConstants.VALIDATE_CODE + key);
//        if (StrUtil.isBlank(codeFromSystem)){
//            throw new ServiceException("验证码校验失败，未发送验证码");
//        }
//        if (!code.equals(codeFromSystem)){
//            throw new ServiceException("验证码校验失败，验证码错误");
//        }
//        return true;
//    }
    public void checkPhoneByRegex(String phone){
        if (!ReUtil.isMatch(RegexPool.MOBILE,phone)) {
            throw new ServiceException("手机号格式错误");
        }
    }
    public void checkEmailByRegex(String email){
        if (!ReUtil.isMatch(RegexPool.EMAIL,email)) {
            throw new ServiceException("邮箱格式错误");
        }
    }
}
