package com.common.message.config;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * One-way activation for RocketMQ listeners deferred during blue-green startup.
 */
@Component
public class RocketMQConsumerActivationManager {

    public static final String MQ_ACTIVE_COLOR_PROPERTY = "release.mq.active-color";
    public static final String LOCAL_COLOR_PROPERTY = "release.color";

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerActivationManager.class);
    private static final String ACTIVATION_ROLLBACK_TIMEOUT_MILLIS_PROPERTY =
            "erp.mq.consumer.activation-rollback-timeout-ms";
    private static final long DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS = 60000L;

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final ListenerRegistrar listenerRegistrar;
    private final RocketMQConsumerLifecycleCoordinator lifecycleCoordinator;
    private final AsyncTaskExecutor rollbackExecutor;
    private final RocketMQConsumerEnabledResolver.ConsumerSwitchState consumerSwitchState;
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
     * @param rollbackExecutor bounded executor used to stop partially activated listeners
     */
    @Autowired
    public RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                             Environment environment,
                                             RocketMQMessageConverter messageConverter,
                                             RocketMQProperties rocketMQProperties,
                                             RocketMQConsumerLifecycleCoordinator lifecycleCoordinator,
                                             @Qualifier(MessageLifecycleExecutorConfig.ROCKETMQ_CONTAINER_EXECUTOR)
                                                     AsyncTaskExecutor rollbackExecutor) {
        this(applicationContext, environment, () -> {
            if (!(environment instanceof StandardEnvironment)) {
                throw new IllegalStateException("Spring Environment is not a StandardEnvironment");
            }
            ListenerContainerConfiguration configuration = new ListenerContainerConfiguration(
                    messageConverter, (StandardEnvironment) environment, rocketMQProperties);
            configuration.setApplicationContext(applicationContext);
            configuration.afterSingletonsInstantiated();
        }, lifecycleCoordinator, rollbackExecutor);
    }

    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar) {
        this(applicationContext,
                environment,
                listenerRegistrar,
                new RocketMQConsumerLifecycleCoordinator(),
                new TaskExecutorAdapter(Runnable::run));
    }

    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar,
                                      RocketMQConsumerLifecycleCoordinator lifecycleCoordinator) {
        this(applicationContext,
                environment,
                listenerRegistrar,
                lifecycleCoordinator,
                new TaskExecutorAdapter(Runnable::run));
    }

    /**
     * Creates an activation manager with an explicit rollback executor.
     *
     * @param applicationContext current application context
     * @param environment Spring environment
     * @param listenerRegistrar deferred listener registrar
     * @param lifecycleCoordinator shared activation and drain coordinator
     * @param rollbackExecutor executor used to stop partially activated listeners
     */
    RocketMQConsumerActivationManager(ApplicationContext applicationContext,
                                      Environment environment,
                                      ListenerRegistrar listenerRegistrar,
                                      RocketMQConsumerLifecycleCoordinator lifecycleCoordinator,
                                      AsyncTaskExecutor rollbackExecutor) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.listenerRegistrar = listenerRegistrar;
        this.lifecycleCoordinator = lifecycleCoordinator;
        this.rollbackExecutor = rollbackExecutor;
        this.consumerSwitchState = RocketMQConsumerEnabledResolver.resolve(environment);
        this.startupEnabled =
                consumerSwitchState == RocketMQConsumerEnabledResolver.ConsumerSwitchState.ENABLED;
        if (startupEnabled) {
            this.activationState = ActivationState.NATIVE;
        } else if (consumerSwitchState == RocketMQConsumerEnabledResolver.ConsumerSwitchState.DEFERRED) {
            this.activationState = ActivationState.DEFERRED;
        } else {
            this.activationState = ActivationState.INVALID;
            this.failureMessage = "invalid RocketMQ consumer startup switch";
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void activateOnApplicationReadyWhenColorIsActive() {
        if (consumerSwitchState == RocketMQConsumerEnabledResolver.ConsumerSwitchState.DEFERRED
                && isColorEligible()) {
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
        if (activationState == ActivationState.INVALID) {
            throw new InvalidConsumerSwitchException(
                    "RocketMQ activation is blocked because the consumer startup switch is invalid");
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

    /**
     * Returns the typed activation state for internal lifecycle decisions.
     *
     * @return current activation state
     */
    public ActivationState getActivationStateValue() {
        return activationState;
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
        Map<String, Future<?>> rollbackFutures = new LinkedHashMap<>();
        long rollbackTimeoutMillis = getActivationRollbackTimeoutMillis();
        long rollbackDeadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(rollbackTimeoutMillis);
        for (Map.Entry<String, DefaultRocketMQListenerContainer> entry : currentContainers.entrySet()) {
            if (containersBeforeActivation.contains(entry.getKey())) {
                continue;
            }
            try {
                String beanName = entry.getKey();
                DefaultRocketMQListenerContainer container = entry.getValue();
                rollbackFutures.put(beanName, rollbackExecutor.submit(
                        () -> stopActivatedContainer(beanName, container, rollbackTimeoutMillis)));
            } catch (RuntimeException | Error submitError) {
                rollbackFailures.add(entry.getKey() + ": "
                        + submitError.getClass().getSimpleName() + ": " + submitError.getMessage());
            }
        }

        for (Map.Entry<String, Future<?>> entry : rollbackFutures.entrySet()) {
            long remainingNanos = rollbackDeadlineNanos - System.nanoTime();
            if (remainingNanos <= 0L) {
                rollbackFailures.add("activation rollback timed out after " + rollbackTimeoutMillis + "ms");
                cancelOutstandingRollbacks(rollbackFutures);
                break;
            }
            try {
                entry.getValue().get(remainingNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                rollbackFailures.add("activation rollback was interrupted");
                cancelOutstandingRollbacks(rollbackFutures);
                break;
            } catch (TimeoutException ex) {
                rollbackFailures.add("activation rollback timed out after " + rollbackTimeoutMillis + "ms");
                cancelOutstandingRollbacks(rollbackFutures);
                break;
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                rollbackFailures.add(entry.getKey() + ": "
                        + cause.getClass().getSimpleName() + ": " + cause.getMessage());
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
     * Stops one listener created by the failed activation attempt.
     *
     * @param beanName listener Bean name
     * @param container listener container
     * @param rollbackTimeoutMillis maximum client shutdown wait
     */
    private void stopActivatedContainer(String beanName,
                                        DefaultRocketMQListenerContainer container,
                                        long rollbackTimeoutMillis) {
        if (container.isRunning()) {
            if (container.getConsumer() != null) {
                container.getConsumer().setAwaitTerminationMillisWhenShutdown(rollbackTimeoutMillis);
            }
            container.stop();
        }
        if (container.isRunning()) {
            throw new IllegalStateException(beanName + " remains running after stop");
        }
    }

    /**
     * Cancels every unfinished rollback task after the shared deadline expires.
     *
     * @param rollbackFutures submitted rollback tasks
     */
    private void cancelOutstandingRollbacks(Map<String, Future<?>> rollbackFutures) {
        for (Future<?> future : rollbackFutures.values()) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }

    /**
     * Resolves the total activation rollback deadline without using strict property conversion.
     *
     * @return positive rollback timeout in milliseconds
     */
    private long getActivationRollbackTimeoutMillis() {
        String configured = environment.getProperty(ACTIVATION_ROLLBACK_TIMEOUT_MILLIS_PROPERTY);
        if (!hasText(configured)) {
            return DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS;
        }
        try {
            long timeout = Long.parseLong(configured.trim());
            if (timeout > 0L) {
                return timeout;
            }
        } catch (NumberFormatException ex) {
            LOGGER.warn("Invalid {} value '{}'; using default {}ms",
                    ACTIVATION_ROLLBACK_TIMEOUT_MILLIS_PROPERTY,
                    configured,
                    DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS);
            return DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS;
        }
        LOGGER.warn("Non-positive {} value '{}'; using default {}ms",
                ACTIVATION_ROLLBACK_TIMEOUT_MILLIS_PROPERTY,
                configured,
                DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS);
        return DEFAULT_ACTIVATION_ROLLBACK_TIMEOUT_MILLIS;
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

    /** Internal listener activation states exposed to the release status endpoint by name. */
    public enum ActivationState {
        NATIVE,
        DEFERRED,
        INVALID,
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

    /** Rejects activation when the startup switch contains an unresolved or malformed value. */
    public static class InvalidConsumerSwitchException extends IllegalStateException {
        /**
         * Creates a controlled rejection for an invalid consumer startup switch.
         *
         * @param message rejection detail retained in internal logs
         */
        public InvalidConsumerSwitchException(String message) {
            super(message);
        }
    }
}
