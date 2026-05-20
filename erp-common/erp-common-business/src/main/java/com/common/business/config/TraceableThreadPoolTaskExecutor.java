package com.common.business.config;

import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 支持 SkyWalking 独立链路追踪的 ThreadPoolTaskExecutor
 * 每个子线程任务都会生成全新的 TraceId（不继承父线程）
 */
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
        // 包装：在子线程中通过 @Trace 方法执行
        super.execute(() -> runWithNewTrace(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(() -> runWithNewTrace(task), startTimeout);
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