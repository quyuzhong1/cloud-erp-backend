package com.common.business.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

public class SkyWalkingTraceIdConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String traceId = TraceContext.traceId();
        // TraceContext.traceId() 本身返回的就是纯 ID 或 N/A
        // 不需要额外处理
        return traceId;
    }
}
