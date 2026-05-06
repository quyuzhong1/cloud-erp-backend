package com.common.business.config;

import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 支持 SkyWalking 全链路追踪的 ThreadPoolTaskExecutor
 * 自动包装所有提交的任务，确保子线程继承父线程的 TraceId
 */
public class TraceableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(Runnable task) {
        // 包装 Runnable
        super.execute(RunnableWrapper.of(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(RunnableWrapper.of(task), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(RunnableWrapper.of(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(CallableWrapper.of(task));
    }
}