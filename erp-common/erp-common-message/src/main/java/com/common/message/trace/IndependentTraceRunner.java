package com.common.message.trace;

import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.Tracer;
import org.springframework.stereotype.Component;

/**
 * 在独立 SkyWalking Trace 中执行任务（生成全新 traceId，不继承当前线程上下文）。
 * <p>
 * 使用 {@link Tracer#createEntrySpan}（carrier=null）强制开 Entry，而不是 {@code @Trace} 的 LocalSpan；
 * 执行前会清空线程上残留 Span，避免复用旧 TraceId。
 */
@Component
public class IndependentTraceRunner {

    private static final String OPERATION_NAME = "rocketmq-consume";
    private static final String TRACE_ID_NA = "N/A";
    private static final int MAX_CLEAR_DEPTH = 64;

    /**
     * @param task 允许抛出任意异常（如 AOP {@code proceed}）
     */
    public Object run(ThrowingSupplier<?> task) throws Throwable {
        clearActiveSpans();
        Tracer.createEntrySpan(OPERATION_NAME, null);
        try {
            return task.get();
        } catch (Throwable t) {
            ActiveSpan.error(t);
            throw t;
        } finally {
            try {
                Tracer.stopSpan();
            } finally {
                clearActiveSpans();
            }
        }
    }

    /**
     * 结束当前线程上已有 Span，使后续 createEntrySpan 拿到全新 TraceId。
     */
    private static void clearActiveSpans() {
        for (int i = 0; i < MAX_CLEAR_DEPTH; i++) {
            String traceId = TraceContext.traceId();
            if (traceId == null || TRACE_ID_NA.equals(traceId)) {
                return;
            }
            try {
                Tracer.stopSpan();
            } catch (Throwable ignore) {
                return;
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
