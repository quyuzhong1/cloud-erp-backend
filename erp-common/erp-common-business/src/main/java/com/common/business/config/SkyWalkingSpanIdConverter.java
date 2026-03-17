package com.common.business.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

public class SkyWalkingSpanIdConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        return TraceContext.segmentId();
    }
}
