package com.cloud.erp.thirdparty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname FsProperties
 * @Description TODO
 * @Date 2022-07-20 17:57
 * @Created by yl
 */
@ConfigurationProperties(prefix = "third.fs")
@Data
@Configuration
public class FsProperties {

    private String clientSecret;

    private String clientId;

    private String redirectLoginUri;

    private String redirectBindingUri;

}
