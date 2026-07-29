package com.common.message.controller.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed response used by RocketMQ release lifecycle endpoints.
 */
@Data
public class RocketMQLifecycleStatusVO {
    private String status;
    private String message;
    private boolean enabled;
    private boolean startupEnabled;
    private String activationState;
    private String mqActiveColor;
    private String localColor;
    private boolean colorEligible;
    private String drainState;
    private int drainTotalContainers;
    private int drainedContainers;
    private String drainFailure;
    private int totalContainers;
    private int runningContainers;
    private List<RocketMQContainerStatusVO> containers = new ArrayList<>();
}
