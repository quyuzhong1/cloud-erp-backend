package com.common.message.controller.vo;

import lombok.Data;

/**
 * Typed response used by XXL-JOB release lifecycle endpoints.
 */
@Data
public class XxlJobLifecycleStatusVO {
    private String status;
    private String message;
    private boolean configured;
    private boolean acceptingTriggers;
    private boolean registryRemovalRequested;
    private int busyJobThreads;
    private String failure;
}
