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
        return traceRunner.run(invocation::proceed);
    }

    private static boolean isOnMessage(MethodInvocation invocation) {
        return "onMessage".equals(invocation.getMethod().getName())
                && invocation.getArguments() != null
                && invocation.getArguments().length == 1;
    }
}
