package com.common.message.config;

import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Terminal drain used immediately before an old blue-green Pod is removed.
 */
@Component
public class RocketMQConsumerDrainManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerDrainManager.class);

    /*
     * RocketMQ 4.9.3 interrupts consumer tasks when its shutdown wait expires.
     * Keep the library wait effectively unbounded; Jenkins may stop waiting,
     * but it must never delete a Pod that has not reached DRAINED.
     */
    private static final long TERMINAL_DRAIN_WAIT_MILLIS = Long.MAX_VALUE;

    private final ApplicationContext applicationContext;

    private volatile DrainState drainState = DrainState.RUNNING;
    private volatile int totalContainers;
    private volatile int drainedContainers;
    private volatile String failureMessage;

    public RocketMQConsumerDrainManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public synchronized void beginDrain() {
        if (drainState == DrainState.DRAINING || drainState == DrainState.DRAINED) {
            return;
        }
        if (drainState == DrainState.FAILED) {
            throw new IllegalStateException("RocketMQ terminal drain has failed: " + failureMessage);
        }

        Map<String, DefaultRocketMQListenerContainer> containerMap = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);
        List<DefaultRocketMQListenerContainer> containers = new ArrayList<>(containerMap.values());
        totalContainers = containers.size();
        drainedContainers = 0;
        failureMessage = null;

        if (containers.isEmpty()) {
            drainState = DrainState.DRAINED;
            return;
        }

        drainState = DrainState.DRAINING;
        Thread coordinator = new Thread(() -> drainAll(containers), "rocketmq-terminal-drain");
        coordinator.setDaemon(true);
        coordinator.start();
    }

    private void drainAll(List<DefaultRocketMQListenerContainer> containers) {
        ExecutorService executor = Executors.newFixedThreadPool(containers.size(), new DrainThreadFactory());
        List<Future<?>> futures = new ArrayList<>();
        for (DefaultRocketMQListenerContainer container : containers) {
            futures.add(executor.submit(() -> drainContainer(container)));
        }
        executor.shutdown();

        String firstFailure = null;
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
        synchronized (this) {
            drainedContainers++;
        }
    }

    public String getDrainState() {
        return drainState.name();
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

    enum DrainState {
        RUNNING,
        DRAINING,
        DRAINED,
        FAILED
    }

    private static class DrainThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "rocketmq-container-drain-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
