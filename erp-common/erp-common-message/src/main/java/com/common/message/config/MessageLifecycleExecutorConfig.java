package com.common.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Bounded Spring-managed executors dedicated to release lifecycle operations.
 */
@Configuration
public class MessageLifecycleExecutorConfig {

    public static final String COORDINATOR_EXECUTOR = "messageLifecycleCoordinatorExecutor";
    public static final String ROCKETMQ_CONTAINER_EXECUTOR = "rocketMqContainerDrainExecutor";

    /**
     * Executes the small number of MQ and XXL-JOB drain coordinators.
     *
     * @return bounded lifecycle coordinator executor
     */
    @Bean(name = COORDINATOR_EXECUTOR)
    public ThreadPoolTaskExecutor messageLifecycleCoordinatorExecutor() {
        return executor("message-lifecycle-", 2, 4, 32);
    }

    /**
     * Stops RocketMQ listener containers with bounded parallelism.
     *
     * @return bounded RocketMQ container drain executor
     */
    @Bean(name = ROCKETMQ_CONTAINER_EXECUTOR)
    public ThreadPoolTaskExecutor rocketMqContainerDrainExecutor() {
        return executor("rocketmq-container-drain-", 4, 8, 64);
    }

    /**
     * Creates an executor that waits for accepted lifecycle work during Spring shutdown.
     *
     * @param threadNamePrefix thread name prefix
     * @param corePoolSize core thread count
     * @param maxPoolSize maximum thread count
     * @param queueCapacity bounded queue capacity
     * @return configured executor
     */
    private ThreadPoolTaskExecutor executor(String threadNamePrefix,
                                            int corePoolSize,
                                            int maxPoolSize,
                                            int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        // Rejection must be visible to the drain state machine; silent loss would leave it stuck at DRAINING.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
