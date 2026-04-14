package com.common.business.config;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 可自动传递 SkyWalking Trace 上下文的线程池包装器
 * 将所有任务提交方法（execute, submit, invokeAll, invokeAny）中的 Runnable/Callable 进行包装，
 * 确保子线程能够继承父线程的 TraceId。
 */
public class TraceableExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    public TraceableExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(RunnableWrapper.of(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(RunnableWrapper.of(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(RunnableWrapper.of(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(CallableWrapper.of(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        // 包装每个 Callable
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

    // 辅助方法：包装 Callable 集合
    private <T> Collection<Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            wrapped.add(CallableWrapper.of(task));
        }
        return wrapped;
    }
}
