package blossom.project.towelove.client.serivce;

import blossom.project.towelove.client.fallback.RemoteEmailFallbackFactory;
import blossom.project.towelove.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;

@FeignClient(value = "towelove-msg",
        fallbackFactory = RemoteEmailFallbackFactory.class,
        contextId = "RemoteEmailService")
public interface RemoteCodeService {

    /**
     * 生成验证码
     *
     * @param email
     * @return
     */
    @GetMapping("/v1/email/code")
    Result<String> sendValidateCodeByEmail(@NotBlank @RequestParam(name = "email")String email);

    @GetMapping("/v1/sms")
    Result<String> sendValidateCodeByPhone(@NotBlank @RequestParam("phone")String phoneNumber);

}
