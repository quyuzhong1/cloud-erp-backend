package com.common.message.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 蓝绿发布时通过 Nacos 动态控制 XXL-JOB 执行器注册，避免新旧版本同时执行定时任务。
 */
public class ReleaseControlledXxlJobSpringExecutor extends XxlJobSpringExecutor
        implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

    private static final Logger log = LoggerFactory.getLogger(ReleaseControlledXxlJobSpringExecutor.class);
    private static final String XXL_JOB_ENABLED_KEY = "release.xxl.job.enabled";
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String LOCAL_COLOR_KEY = "release.color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private static final String LOCAL_VERSION_KEY = "release.version";
    private static final String ENVIRONMENT_CHANGE_EVENT = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";

    private final AtomicBoolean executorStarted = new AtomicBoolean(false);
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (isXxlJobEnabled()) {
            startExecutor();
            return;
        }
        log.warn(">>>>>>>>>>> xxl-job executor disabled by {}=false.", XXL_JOB_ENABLED_KEY);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (ENVIRONMENT_CHANGE_EVENT.equals(event.getClass().getName())) {
            refreshExecutorState();
        }
    }

    @Override
    public void destroy() {
        stopExecutor();
    }

    private boolean isXxlJobEnabled() {
        if (environment == null) {
            return true;
        }
        return environment.getProperty(XXL_JOB_ENABLED_KEY, Boolean.class, true)
                && isCurrentReleaseActive();
    }

    private boolean isCurrentReleaseActive() {
        String activeColor = environment.getProperty(ACTIVE_COLOR_KEY);
        String localColor = environment.getProperty(LOCAL_COLOR_KEY);
        if (hasText(activeColor) && hasText(localColor)) {
            return activeColor.equalsIgnoreCase(localColor);
        }

        String activeVersion = environment.getProperty(ACTIVE_VERSION_KEY);
        String localVersion = environment.getProperty(LOCAL_VERSION_KEY);
        if (hasText(activeVersion) && hasText(localVersion)) {
            return activeVersion.equalsIgnoreCase(localVersion);
        }

        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private synchronized void refreshExecutorState() {
        if (isXxlJobEnabled()) {
            startExecutor();
        } else {
            stopExecutor();
        }
    }

    private void startExecutor() {
        if (!executorStarted.compareAndSet(false, true)) {
            return;
        }
        try {
            super.afterSingletonsInstantiated();
            log.warn(">>>>>>>>>>> xxl-job executor started by {}=true.", XXL_JOB_ENABLED_KEY);
        } catch (RuntimeException e) {
            executorStarted.set(false);
            throw e;
        }
    }

    private void stopExecutor() {
        if (!executorStarted.compareAndSet(true, false)) {
            return;
        }
        super.destroy();
        log.warn(">>>>>>>>>>> xxl-job executor stopped by {}=false.", XXL_JOB_ENABLED_KEY);
    }
}
