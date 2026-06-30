package com.common.business.config;

import org.apache.skywalking.apm.toolkit.trace.Trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 支持 SkyWalking 独立链路追踪的线程池包装器
 * 每个子线程任务都会生成全新的 TraceId（不继承父线程）
 */
public class TraceableExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    public TraceableExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    // ========== @Trace 方法：为任务创建全新的 Trace 链路 ==========

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

    // ========== 核心任务提交方法（包装为独立链路） ==========

    @Override
    public void execute(Runnable command) {
        // 包装：在子线程中通过 @Trace 方法执行
        delegate.execute(() -> runWithNewTrace(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        // 将 Runnable 包装为 Callable，内部调用 @Trace 方法
        return delegate.submit(() -> {
            runWithNewTrace(task);
            return null;
        });
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(() -> {
            runWithNewTrace(task);
            return result;
        });
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(() -> callWithNewTrace(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        // 包装每个 Callable，使其在独立 Trace 中执行
        Collection<Callable<T>> wrappedTasks = wrapCallableCollection(tasks);
        return delegate.invokeAll(wrappedTasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        Collection<Callable<T>> wrappedTasks = wrapCallableCollection(tasks);
        return delegate.invokeAll(wrappedTasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        Collection<Callable<T>> wrappedTasks = wrapCallableCollection(tasks);
        return delegate.invokeAny(wrappedTasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        Collection<Callable<T>> wrappedTasks = wrapCallableCollection(tasks);
        return delegate.invokeAny(wrappedTasks, timeout, unit);
    }

    // ========== 委托方法（直接透传） ==========

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    // ========== 辅助方法 ==========

    /**
     * 包装 Callable 集合，使每个任务在独立的 Trace 中执行
     */
    private <T> Collection<Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            wrapped.add(() -> callWithNewTrace(task));
        }
        return wrapped;
    }
}