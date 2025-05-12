package com.erp.approve.fs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname FsProperties

 * @Date 2022-07-20 17:57
 * @Created by yl
 */
@Data
@ConfigurationProperties(prefix = "third.fs")
@Configuration
public class FsApproveProperties {

    private String appId;

    private String appSecret;
}
