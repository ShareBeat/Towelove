package com.towelove.system.controller.user;

import com.towelove.common.core.constant.UserConstants;
import com.towelove.common.core.domain.R;
import com.towelove.common.minio.MinioService;
import com.towelove.system.api.domain.SysUser;
import com.towelove.system.domain.user.UserInfoBaseVO;
import com.towelove.system.service.user.ISysUserService;
import com.towelove.system.service.user.UserInfoService;
import io.minio.GetObjectResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: 张锦标
 * @date: 2023/4/9 20:34
 * UserInfoController类
 * 当前类用于提供用户当前账号自己信息的
 * 增删改查功能
 */
@RestController
@RequestMapping("/sys/user/info")
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private MinioService minioService;
    /**
     * 用户注册用户信息
     */
    @PostMapping("/register")
    //这里重写最好
    public R<Boolean> register(@RequestBody SysUser sysUser)
    {
        String username = sysUser.getUserName();
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(sysUser)))
        {
            return R.fail("保存用户'" + username + "'失败，用户名已存在");
        }
        return R.ok(userService.registerUser(sysUser));
    }


    /**
     * 判断当前用户输入的旧密码是否正确
     * @param username 用户名
     * @param oldPassword 用户旧密码
     * @return 返回是否正确 true为正确
     */
    @GetMapping("/compare/pwd")
    public R<Boolean> comparePwd(@RequestParam("username") String username,
                                 @RequestParam("oldPassword") String oldPassword){
        return R.ok(userService.comparePwd(username,oldPassword));
    }
    /**
     * 当前方法用于用户上传头像
     *
     * @param userId
     * @param file
     * @return
     */
    @PostMapping("/upload/avatar/{userId}")
    public R<SysUser> uploadAvatar(@PathVariable("userId") Long userId,
                                  MultipartFile file) {
        SysUser sysUser = userInfoService.uploadAvatar(userId, file);
        return R.ok(sysUser);
    }

    /**
     * 当前方法用于完成当前用户的头像的获取
     *
     * @param userId   用户id
     * @param response
     */
    @GetMapping("/download/avatar/{userId}")
    public void downloadAvatar(@PathVariable("userId") Long userId,
                               HttpServletResponse response) {
        try (ServletOutputStream os = response.getOutputStream()) {
            response.setContentType("image/jpeg");
            GetObjectResponse file = userInfoService.downloadAvatar(userId);
            int len = 0;
            byte[] buffre = new byte[1024 * 10];
            while ((len = file.read(buffre)) != -1) {
                os.write(buffre, 0, len);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户修改用户信息
     * @param baseVO 表单提交过来的用户信息
     * @return 返回修改后的用户信息
     */
    @PutMapping("/edit")
    public R<SysUser> edit(@RequestBody UserInfoBaseVO baseVO) {
        SysUser sysUser = userInfoService.updateUserInfo(baseVO);
        sysUser.setPassword(null);
        return R.ok(sysUser);
    }
}
