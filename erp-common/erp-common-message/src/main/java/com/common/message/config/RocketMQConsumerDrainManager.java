package com.common.message.config;

import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Terminal drain used immediately before an old blue-green Pod is removed.
 */
@Component
public class RocketMQConsumerDrainManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerDrainManager.class);

    /*
     * RocketMQ 4.9.3 interrupts consumer tasks when its shutdown wait expires.
     * Keep the library wait effectively unbounded so this process does not interrupt an in-flight
     * handler. Jenkins owns the bounded release timeout and may eventually force Pod replacement.
     */
    private static final long TERMINAL_DRAIN_WAIT_MILLIS = Long.MAX_VALUE;

    private final ApplicationContext applicationContext;
    private final RocketMQConsumerLifecycleCoordinator lifecycleCoordinator;
    private final AsyncTaskExecutor coordinatorExecutor;
    private final AsyncTaskExecutor containerDrainExecutor;

    private volatile DrainState drainState = DrainState.RUNNING;
    private volatile int totalContainers;
    private volatile int drainedContainers;
    private volatile String failureMessage;

    /**
     * Creates the terminal drain manager with bounded Spring-managed executors.
     *
     * @param applicationContext current application context
     * @param lifecycleCoordinator shared activation and drain coordinator
     * @param coordinatorExecutor drain coordination executor
     * @param containerDrainExecutor listener container drain executor
     */
    public RocketMQConsumerDrainManager(
            ApplicationContext applicationContext,
            RocketMQConsumerLifecycleCoordinator lifecycleCoordinator,
            @Qualifier(MessageLifecycleExecutorConfig.COORDINATOR_EXECUTOR)
                    AsyncTaskExecutor coordinatorExecutor,
            @Qualifier(MessageLifecycleExecutorConfig.ROCKETMQ_CONTAINER_EXECUTOR)
                    AsyncTaskExecutor containerDrainExecutor) {
        this.applicationContext = applicationContext;
        this.lifecycleCoordinator = lifecycleCoordinator;
        this.coordinatorExecutor = coordinatorExecutor;
        this.containerDrainExecutor = containerDrainExecutor;
    }

    public synchronized void beginDrain() {
        if (drainState == DrainState.DRAINING || drainState == DrainState.DRAINED) {
            return;
        }
        if (drainState == DrainState.FAILED) {
            throw new IllegalStateException("RocketMQ terminal drain has failed: " + failureMessage);
        }

        RocketMQConsumerLifecycleCoordinator.TerminalDrainPermit<List<DefaultRocketMQListenerContainer>> permit;
        try {
            permit = lifecycleCoordinator.beginTerminalDrain(this::listenerSnapshot);
        } catch (RuntimeException | Error ex) {
            failureMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            drainState = DrainState.FAILED;
            throw ex;
        }
        if (permit == null) {
            return;
        }
        List<DefaultRocketMQListenerContainer> containers = permit.getSnapshot();
        totalContainers = containers.size();
        drainedContainers = 0;
        failureMessage = null;

        if (containers.isEmpty()) {
            drainState = DrainState.DRAINED;
            return;
        }

        drainState = DrainState.DRAINING;
        try {
            coordinatorExecutor.submit(() -> drainAll(containers));
        } catch (RuntimeException ex) {
            failureMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            drainState = DrainState.FAILED;
            throw ex;
        }
    }

    /**
     * Captures every listener container while activation is blocked by the lifecycle coordinator.
     *
     * @return complete listener container snapshot
     */
    private List<DefaultRocketMQListenerContainer> listenerSnapshot() {
        Map<String, DefaultRocketMQListenerContainer> containerMap = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);
        return new ArrayList<>(containerMap.values());
    }

    private void drainAll(List<DefaultRocketMQListenerContainer> containers) {
        List<Future<?>> futures = new ArrayList<>();
        String firstFailure = null;
        for (DefaultRocketMQListenerContainer container : containers) {
            try {
                futures.add(containerDrainExecutor.submit(() -> drainContainer(container)));
            } catch (RuntimeException ex) {
                firstFailure = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                break;
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                firstFailure = "terminal drain coordinator was interrupted";
                break;
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                if (firstFailure == null) {
                    firstFailure = cause.getClass().getSimpleName() + ": " + cause.getMessage();
                }
            }
        }

        if (firstFailure == null && drainedContainers == totalContainers) {
            drainState = DrainState.DRAINED;
            LOGGER.info("RocketMQ consumers drained before Pod removal: total={}", totalContainers);
        } else {
            failureMessage = firstFailure == null
                    ? "not all RocketMQ listener containers completed terminal drain"
                    : firstFailure;
            drainState = DrainState.FAILED;
            LOGGER.error("RocketMQ terminal drain failed: total={}, drained={}, reason={}",
                    totalContainers, drainedContainers, failureMessage);
        }
    }

    private void drainContainer(DefaultRocketMQListenerContainer container) {
        if (container.getConsumer() != null) {
            container.getConsumer().setAwaitTerminationMillisWhenShutdown(TERMINAL_DRAIN_WAIT_MILLIS);
        }
        container.stop();
        if (container.isRunning()) {
            throw new IllegalStateException("RocketMQ listener container remains running after stop: "
                    + container.getConsumerGroup() + "/" + container.getTopic());
        }
        synchronized (this) {
            drainedContainers++;
        }
    }

    public String getDrainState() {
        return drainState.name();
    }

    /**
     * Returns the typed drain state for internal lifecycle decisions.
     *
     * @return current terminal drain state
     */
    public DrainState getDrainStateValue() {
        return drainState;
    }

    public int getTotalContainers() {
        return totalContainers;
    }

    public int getDrainedContainers() {
        return drainedContainers;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    /** Terminal consumer drain states exposed to the release status endpoint by name. */
    public enum DrainState {
        RUNNING,
        DRAINING,
        DRAINED,
        FAILED
    }

}
