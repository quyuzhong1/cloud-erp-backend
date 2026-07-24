package com.common.message.trace;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 拦截 {@code onMessage}，在独立 SkyWalking Trace 中执行；通过 CGLIB 代理挂载以保留原消费者类型。
 */
public class IndependentTraceRocketMQListenerAdvice implements MethodInterceptor {

    private final IndependentTraceRunner traceRunner;

    public IndependentTraceRocketMQListenerAdvice(IndependentTraceRunner traceRunner) {
        this.traceRunner = traceRunner;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!isOnMessage(invocation)) {
            return invocation.proceed();
        }
        ThrowableHolder holder = new ThrowableHolder();
        traceRunner.run(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                holder.throwable = t;
            }
        });
        if (holder.throwable != null) {
            throw holder.throwable;
        }
        return null;
    }

    private static boolean isOnMessage(MethodInvocation invocation) {
        return "onMessage".equals(invocation.getMethod().getName())
                && invocation.getArguments() != null
                && invocation.getArguments().length == 1;
    }

    private static final class ThrowableHolder {
        private Throwable throwable;
    }
}
