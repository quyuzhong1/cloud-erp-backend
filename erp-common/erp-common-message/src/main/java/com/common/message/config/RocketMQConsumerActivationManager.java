package com.common.message.config;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * One-way activation for RocketMQ listeners deferred during blue-green startup.
 */
@Component
public class RocketMQConsumerActivationManager {

    public static final String MQ_ACTIVE_COLOR_PROPERTY = "release.mq.active-color";
    public static final String LOCAL_COLOR_PROPERTY = "release.color";

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerActivationManager.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final ListenerRegistrar listenerRegistrar;
    private final boolean startupEnabled;

    private volatile ActivationState activationState;
    private volatile String failureMessage;

    @Autowired
    public RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                             Environment environment,
                                             RocketMQMessageConverter messageConverter,
                                             RocketMQProperties rocketMQProperties) {
        this(applicationContext, environment, () -> {
            if (!(environment instanceof StandardEnvironment)) {
                throw new IllegalStateException("Spring Environment is not a StandardEnvironment");
            }
            ListenerContainerConfiguration configuration = new ListenerContainerConfiguration(
                    messageConverter, (StandardEnvironment) environment, rocketMQProperties);
            configuration.setApplicationContext(applicationContext);
            configuration.afterSingletonsInstantiated();
        });
    }

    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.listenerRegistrar = listenerRegistrar;
        this.startupEnabled = environment.getProperty(
                RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, Boolean.class, Boolean.TRUE);
        this.activationState = startupEnabled ? ActivationState.NATIVE : ActivationState.DEFERRED;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void activateOnApplicationReadyWhenColorIsActive() {
        if (!startupEnabled && isColorEligible()) {
            activate();
        }
    }

    public synchronized void activate() {
        if (startupEnabled || activationState == ActivationState.ACTIVE) {
            return;
        }
        if (activationState == ActivationState.FAILED) {
            throw new IllegalStateException("RocketMQ activation has failed and cannot be retried in this process: "
                    + failureMessage);
        }
        if (!isColorEligible()) {
            throw new ActivationNotEligibleException("MQ active color does not match local color: mqActiveColor="
                    + getMqActiveColor() + ", localColor=" + getLocalColor());
        }

        activationState = ActivationState.ACTIVATING;
        try {
            listenerRegistrar.register();
            ContainerSummary summary = containerSummary();
            if (summary.getTotal() == 0) {
                activationState = ActivationState.ACTIVE;
                LOGGER.info("No RocketMQ listeners found; deferred activation completed as a no-op");
                return;
            }
            if (summary.getRunning() != summary.getTotal()) {
                throw new IllegalStateException("RocketMQ listener activation incomplete: total="
                        + summary.getTotal() + ", running=" + summary.getRunning());
            }
            activationState = ActivationState.ACTIVE;
            LOGGER.info("RocketMQ listeners activated without restarting the application: total={}", summary.getTotal());
        } catch (RuntimeException | Error ex) {
            activationState = ActivationState.FAILED;
            failureMessage = ex.getMessage();
            throw ex;
        }
    }

    public boolean isStartupEnabled() {
        return startupEnabled;
    }

    public boolean isEffectivelyEnabled() {
        return startupEnabled || activationState == ActivationState.ACTIVE;
    }

    public boolean isColorEligible() {
        String mqActiveColor = getMqActiveColor();
        String localColor = getLocalColor();
        return hasText(mqActiveColor) && hasText(localColor) && mqActiveColor.equalsIgnoreCase(localColor);
    }

    public String getMqActiveColor() {
        return environment.getProperty(MQ_ACTIVE_COLOR_PROPERTY);
    }

    public String getLocalColor() {
        return environment.getProperty(LOCAL_COLOR_PROPERTY);
    }

    public String getActivationState() {
        return activationState.name();
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public ContainerSummary containerSummary() {
        Map<String, DefaultRocketMQListenerContainer> containers = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);
        int running = 0;
        for (DefaultRocketMQListenerContainer container : containers.values()) {
            if (container.isRunning()) {
                running++;
            }
        }
        return new ContainerSummary(containers.size(), running);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    enum ActivationState {
        NATIVE,
        DEFERRED,
        ACTIVATING,
        ACTIVE,
        FAILED
    }

    interface ListenerRegistrar {
        void register();
    }

    public static class ContainerSummary {
        private final int total;
        private final int running;

        ContainerSummary(int total, int running) {
            this.total = total;
            this.running = running;
        }

        public int getTotal() {
            return total;
        }

        public int getRunning() {
            return running;
        }
    }

    public static class ActivationNotEligibleException extends IllegalStateException {
        public ActivationNotEligibleException(String message) {
            super(message);
        }
    }
}
