package com.common.message.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 公共 XXL-JOB 执行器配置。
 */
@Configuration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@Slf4j
public class XxlJobConfig {

    private static final String DEV = "dev";
    private static final String ARCHIVE = "archive";

    @Value("${xxl.job.admin.addresses:}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken:}")
    private String accessToken;

    @Value("${xxl.job.executor.appname:}")
    private String appname;

    @Value("${xxl.job.executor.address:}")
    private String address;

    @Value("${xxl.job.executor.ip:}")
    private String ip;

    @Value("${xxl.job.executor.port:0}")
    private int port;

    @Value("${xxl.job.executor.logpath:}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays:30}")
    private int logRetentionDays;

    @Value("${spring.cloud.nacos.discovery.namespace:dev}")
    private String namespace;

    @Bean
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    public XxlJobSpringExecutor xxlJobExecutor() {
        if (DEV.equalsIgnoreCase(namespace) || isArchiveNamespace()) {
            return null;
        }
        if (!hasText(adminAddresses) || !hasText(appname)) {
            log.info(">>>>>>>>>>> xxl-job config skipped because admin addresses or appname is empty.");
            return null;
        }
        log.info(">>>>>>>>>>> xxl-job config init.");
        log.info(">>>>>>>>>>> xxl-job [adminAddress]={},[appname]={},[accessToken]={}", adminAddresses, appname, accessToken);
        XxlJobSpringExecutor xxlJobSpringExecutor = new ReleaseControlledXxlJobSpringExecutor();
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

    private boolean isArchiveNamespace() {
        return namespace != null && namespace.toLowerCase().contains(ARCHIVE);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
