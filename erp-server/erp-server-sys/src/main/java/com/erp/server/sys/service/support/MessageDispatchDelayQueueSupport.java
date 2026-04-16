package com.erp.server.sys.service.support;

import com.erp.server.sys.service.MessageDispatchTaskService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MessageDispatchDelayQueueSupport {

    private static final String QUEUE_NAME = "sys:message:dispatch:delay:queue";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MessageDispatchTaskService messageDispatchTaskService;

    private final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "message-dispatch-delay-consumer");
            thread.setDaemon(true);
            return thread;
        }
    });

    @PostConstruct
    public void startConsumer() {
        consumerExecutor.submit(() -> {
            RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String taskId = blockingQueue.take();
                    messageDispatchTaskService.executeTaskAsync(taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Message dispatch delay queue consumer interrupted");
                } catch (Exception e) {
                    log.error("Consume delayed message dispatch task failed", e);
                }
            }
        });
    }

    @PreDestroy
    public void stopConsumer() {
        consumerExecutor.shutdownNow();
    }

    public void offer(String taskId, LocalDateTime executeTime) {
        if (taskId == null || executeTime == null) {
            return;
        }
        long delayMs = Math.max(Duration.between(LocalDateTime.now(), executeTime).toMillis(), 0L);
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(taskId, delayMs, TimeUnit.MILLISECONDS);
    }
}
