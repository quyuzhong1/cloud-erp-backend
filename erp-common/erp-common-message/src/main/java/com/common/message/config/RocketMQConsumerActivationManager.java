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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final RocketMQConsumerLifecycleCoordinator lifecycleCoordinator;
    private final boolean startupEnabled;

    private volatile ActivationState activationState;
    private volatile String failureMessage;

    /**
     * Creates the production activation manager backed by RocketMQ's listener registrar.
     *
     * @param applicationContext current application context
     * @param environment Spring environment
     * @param messageConverter RocketMQ message converter
     * @param rocketMQProperties RocketMQ client properties
     * @param lifecycleCoordinator shared activation and drain coordinator
     */
    @Autowired
    public RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                             Environment environment,
                                             RocketMQMessageConverter messageConverter,
                                             RocketMQProperties rocketMQProperties,
                                             RocketMQConsumerLifecycleCoordinator lifecycleCoordinator) {
        this(applicationContext, environment, () -> {
            if (!(environment instanceof StandardEnvironment)) {
                throw new IllegalStateException("Spring Environment is not a StandardEnvironment");
            }
            ListenerContainerConfiguration configuration = new ListenerContainerConfiguration(
                    messageConverter, (StandardEnvironment) environment, rocketMQProperties);
            configuration.setApplicationContext(applicationContext);
            configuration.afterSingletonsInstantiated();
        }, lifecycleCoordinator);
    }

    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar) {
        this(applicationContext, environment, listenerRegistrar, new RocketMQConsumerLifecycleCoordinator());
    }

    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar,
                                      RocketMQConsumerLifecycleCoordinator lifecycleCoordinator) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.listenerRegistrar = listenerRegistrar;
        this.lifecycleCoordinator = lifecycleCoordinator;
        this.startupEnabled = RocketMQConsumerEnabledResolver.isEnabled(environment);
        this.activationState = startupEnabled ? ActivationState.NATIVE : ActivationState.DEFERRED;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void activateOnApplicationReadyWhenColorIsActive() {
        if (!startupEnabled && isColorEligible()) {
            try {
                activate();
            } catch (RocketMQConsumerLifecycleCoordinator.TerminalDrainStartedException ex) {
                // A terminal drain won the race; ApplicationReady must not fail the whole application.
                LOGGER.warn("Skip RocketMQ ApplicationReady activation because terminal drain has started");
            }
        }
    }

    public void activate() {
        lifecycleCoordinator.runActivation(this::activateUnderLifecycleLock);
    }

    /**
     * Registers all deferred listeners while the shared lifecycle lock remains held.
     */
    private synchronized void activateUnderLifecycleLock() {
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
        Set<String> containersBeforeActivation = listenerContainerNames();
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
            String rollbackFailure = rollbackActivatedContainers(containersBeforeActivation);
            activationState = ActivationState.FAILED;
            failureMessage = activationFailureMessage(ex, rollbackFailure);
            if (rollbackFailure != null) {
                ex.addSuppressed(new IllegalStateException(rollbackFailure));
            }
            throw ex;
        }
    }

    public boolean isStartupEnabled() {
        return startupEnabled;
    }

    public boolean isEffectivelyEnabled() {
        return !lifecycleCoordinator.isTerminalDrainStarted()
                && (startupEnabled || activationState == ActivationState.ACTIVE);
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

    /**
     * Captures listener Bean names before deferred registration starts.
     *
     * @return listener Bean names that must not be touched by activation rollback
     */
    private Set<String> listenerContainerNames() {
        return new HashSet<>(applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false).keySet());
    }

    /**
     * Stops every listener created by the failed activation attempt.
     *
     * @param containersBeforeActivation listener Bean names present before activation
     * @return combined rollback failure, or null when every new listener is stopped
     */
    private String rollbackActivatedContainers(Set<String> containersBeforeActivation) {
        Map<String, DefaultRocketMQListenerContainer> currentContainers = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);
        List<String> rollbackFailures = new ArrayList<>();
        for (Map.Entry<String, DefaultRocketMQListenerContainer> entry : currentContainers.entrySet()) {
            if (containersBeforeActivation.contains(entry.getKey())) {
                continue;
            }
            DefaultRocketMQListenerContainer container = entry.getValue();
            try {
                if (container.isRunning()) {
                    if (container.getConsumer() != null) {
                        container.getConsumer().setAwaitTerminationMillisWhenShutdown(Long.MAX_VALUE);
                    }
                    container.stop();
                }
                if (container.isRunning()) {
                    rollbackFailures.add(entry.getKey() + " remains running after stop");
                }
            } catch (RuntimeException | Error rollbackError) {
                rollbackFailures.add(entry.getKey() + ": "
                        + rollbackError.getClass().getSimpleName() + ": " + rollbackError.getMessage());
            }
        }
        if (rollbackFailures.isEmpty()) {
            LOGGER.warn("RocketMQ activation failed; all listeners created by this attempt were stopped");
            return null;
        }
        String rollbackFailure = "RocketMQ activation rollback failed: " + String.join("; ", rollbackFailures);
        LOGGER.error(rollbackFailure);
        return rollbackFailure;
    }

    /**
     * Combines the activation error with any rollback failure for status reporting.
     *
     * @param activationError original activation error
     * @param rollbackFailure rollback failure, may be null
     * @return persistent activation failure detail
     */
    private String activationFailureMessage(Throwable activationError, String rollbackFailure) {
        String activationFailure = activationError.getClass().getSimpleName() + ": " + activationError.getMessage();
        return rollbackFailure == null ? activationFailure : activationFailure + "; " + rollbackFailure;
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
