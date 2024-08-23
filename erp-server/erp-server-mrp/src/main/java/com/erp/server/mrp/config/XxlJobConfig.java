package com.erp.server.mrp.config;

import com.common.business.constant.BusinessCommonConstants;
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
public class XxlJobConfig {


    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address}")
    private String address;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;
    
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        // 开发环境命名空间忽略注入
        if (BusinessCommonConstants.DEV.equalsIgnoreCase(namespace)){
            return null;
        }
        log.info(">>>>>>>>>>> xxl-job config init.");
        log.info(">>>>>>>>>>> xxl-job [adminAddress]={},[appname]={},[accessToken]={}",adminAddresses,appname,accessToken);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}
