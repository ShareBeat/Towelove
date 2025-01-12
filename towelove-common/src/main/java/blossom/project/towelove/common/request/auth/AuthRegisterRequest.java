package blossom.project.towelove.common.request.auth;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class AuthRegisterRequest extends AuthLoginRequest{

    @NotBlank
    private String password;

    @NotBlank
    private String userName;

    @NotBlank
    private String sex;
}
