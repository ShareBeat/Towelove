package com.towelove.common.core.domain;

import lombok.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: 张锦标
 * @date: 2023/3/7 18:05
 * MailConfiguration类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
@ConfigurationProperties(prefix = "spring.mail")
@AutoConfiguration
public class OfficialMailInfo {
    private Integer port;
    private String host;
    private String username;
    private String password;
    private String protocol;


}