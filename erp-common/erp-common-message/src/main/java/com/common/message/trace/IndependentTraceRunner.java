package com.common.message.trace;

import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.stereotype.Component;

/**
 * 在独立 SkyWalking Trace 中执行任务（生成全新 traceId，不继承当前线程上下文）。
 */
@Component
public class IndependentTraceRunner {

    @Trace(operationName = "rocketmq-consume")
    public void run(Runnable task) {
        task.run();
    }
}
