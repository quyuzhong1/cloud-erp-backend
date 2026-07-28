package com.common.business.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 支持 SkyWalking 独立链路追踪的 ThreadPoolTaskExecutor
 * 每个子线程任务都会生成全新的 TraceId（不继承父线程）
 */
@Slf4j
public class TraceableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    /**
     * 为 Runnable 任务创建全新的 Trace 链路
     */
    @Trace(operationName = "async-runnable")
    private void runWithNewTrace(Runnable task) {
        task.run();
    }

    /**
     * 为 Callable 任务创建全新的 Trace 链路
     */
    @Trace(operationName = "async-callable")
    private <T> T callWithNewTrace(Callable<T> task) throws Exception {
        return task.call();
    }

    @Override
    public void execute(Runnable task) {
        // 同 TraceableExecutorService：消化异常，避免 CallerRunsPolicy 冒泡到调度线程
        super.execute(() -> runSafely(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(() -> runSafely(task), startTimeout);
    }

    private void runSafely(Runnable task) {
        try {
            runWithNewTrace(task);
        } catch (Throwable t) {
            log.warn("TraceableThreadPoolTaskExecutor async task failed", t);
            Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
            if (handler != null) {
                try {
                    handler.uncaughtException(Thread.currentThread(), t);
                } catch (Throwable ignored) {
                    // 避免 handler 再次抛出影响提交方
                }
            }
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        // 将 Runnable 包装为 Callable，其中调用 @Trace 方法
        return super.submit(() -> {
            runWithNewTrace(task);
            return null;
        });
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(() -> callWithNewTrace(task));
    }

}