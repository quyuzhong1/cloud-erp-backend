package com.erp.server.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname JwtProperties
 * @Description TODO
 * @Date 2022-07-11 10:22
 * @Created by yl
 */
@ConfigurationProperties(prefix = "auth.jwt")
@Data
@Configuration
public class AuthJwtProperties {
    private String secret;

    private Long expire;

    private String header;

}
