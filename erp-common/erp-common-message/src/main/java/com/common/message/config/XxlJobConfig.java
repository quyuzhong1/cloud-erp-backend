package com.common.message.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 公共 XXL-JOB 执行器配置，替代各业务模块原本重复的 XxlJobConfig。
 * 业务服务启动类已扫描 com.common 包，dev/archive 和配置缺失时继续不注册执行器。
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

    // 保留 dev/archive 和配置缺失时不注册执行器的语义，避免 @Bean 方法返回 null。
    @Bean
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    @Conditional(XxlJobExecutorCondition.class)
    public XxlJobSpringExecutor xxlJobExecutor(
            @Qualifier(MessageLifecycleExecutorConfig.COORDINATOR_EXECUTOR)
                    AsyncTaskExecutor lifecycleExecutor) {
        log.info(">>>>>>>>>>> xxl-job config init.");
        log.info(">>>>>>>>>>> xxl-job [adminAddress]={},[appname]={},[accessTokenConfigured]={}",
                adminAddresses, appname, hasText(accessToken));
        XxlJobSpringExecutor xxlJobSpringExecutor = new ReleaseControlledXxlJobSpringExecutor(lifecycleExecutor);
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

    private static boolean isArchiveNamespace(String namespace) {
        return namespace != null && namespace.toLowerCase().contains(ARCHIVE);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class XxlJobExecutorCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String namespace = context.getEnvironment().getProperty("spring.cloud.nacos.discovery.namespace", DEV);
            if (DEV.equalsIgnoreCase(namespace) || isArchiveNamespace(namespace)) {
                return false;
            }
            return hasText(context.getEnvironment().getProperty("xxl.job.admin.addresses"))
                    && hasText(context.getEnvironment().getProperty("xxl.job.executor.appname"));
        }
    }
}
