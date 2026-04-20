package com.erp.server.sys.service.support;

import com.common.business.constant.RedisCacheConstants;
import com.erp.server.sys.service.MessageDispatchTaskService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
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

    private static final long QUEUED_MARKER_EXTRA_SECONDS = 7200L;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ObjectProvider<MessageDispatchTaskService> messageDispatchTaskServiceProvider;

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
            RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(RedisCacheConstants.SYS_MESSAGE_DISPATCH_DELAY_QUEUE);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String taskId = blockingQueue.take();
                    clearQueuedMarker(taskId);
                    MessageDispatchTaskService messageDispatchTaskService = messageDispatchTaskServiceProvider.getIfAvailable();
                    if (messageDispatchTaskService == null) {
                        log.warn("MessageDispatchTaskService not available, skip delayed task consume, taskId={}", taskId);
                        continue;
                    }
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
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(RedisCacheConstants.SYS_MESSAGE_DISPATCH_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        removeQueuedTask(taskId, blockingQueue, delayedQueue);
        long delayMs = Math.max(Duration.between(LocalDateTime.now(), executeTime).toMillis(), 0L);
        delayedQueue.offer(taskId, delayMs, TimeUnit.MILLISECONDS);
        markQueued(taskId, executeTime);
    }

    public void removeQueuedTask(String taskId) {
        if (taskId == null) {
            return;
        }
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(RedisCacheConstants.SYS_MESSAGE_DISPATCH_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        removeQueuedTask(taskId, blockingQueue, delayedQueue);
    }

    public boolean isQueuedForExecuteTime(String taskId, LocalDateTime executeTime) {
        if (taskId == null || executeTime == null) {
            return false;
        }
        Long queuedEpoch = getQueuedTaskMap().get(taskId);
        return queuedEpoch != null && queuedEpoch.equals(executeTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private void removeQueuedTask(String taskId, RBlockingQueue<String> blockingQueue, RDelayedQueue<String> delayedQueue) {
        boolean removed = false;
        while (delayedQueue.remove(taskId)) {
            removed = true;
        }
        while (blockingQueue.remove(taskId)) {
            removed = true;
        }
        if (removed) {
            log.info("Remove queued delayed message dispatch task, taskId={}", taskId);
        }
        clearQueuedMarker(taskId);
    }

    private void markQueued(String taskId, LocalDateTime executeTime) {
        long executeEpochMs = executeTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long ttlSeconds = Math.max(Duration.between(LocalDateTime.now(), executeTime).getSeconds(), 0L) + QUEUED_MARKER_EXTRA_SECONDS;
        getQueuedTaskMap().fastPut(taskId, executeEpochMs, ttlSeconds, TimeUnit.SECONDS);
    }

    private void clearQueuedMarker(String taskId) {
        getQueuedTaskMap().remove(taskId);
    }

    private RMapCache<String, Long> getQueuedTaskMap() {
        return redissonClient.getMapCache(RedisCacheConstants.SYS_MESSAGE_DISPATCH_DELAY_QUEUED);
    }
}
