package com.erp.server.dmp.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job配置
 * @Date 2022-08-25 11:42
 * @Created by yl
 */
@Configuration
@Slf4j
public class GyyConfig {

    @Value("${gyy.config.appKey}")
    private String appKey;
    @Value("${gyy.config.secretKey}")
    private String secretKey;
    @Value("${gyy.config.sessionKey}")
    private String sessionKey;

}
