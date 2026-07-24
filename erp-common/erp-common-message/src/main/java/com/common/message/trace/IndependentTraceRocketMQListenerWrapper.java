package com.common.message.trace;

import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * 包装 {@link RocketMQListener}，使每次 {@link #onMessage(Object)} 在独立 Trace 中执行。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class IndependentTraceRocketMQListenerWrapper implements RocketMQListener {

    private final RocketMQListener delegate;
    private final IndependentTraceRunner traceRunner;

    public IndependentTraceRocketMQListenerWrapper(RocketMQListener delegate, IndependentTraceRunner traceRunner) {
        this.delegate = delegate;
        this.traceRunner = traceRunner;
    }

    @Override
    public void onMessage(Object message) {
        traceRunner.run(() -> delegate.onMessage(message));
    }
}
