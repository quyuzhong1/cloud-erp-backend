package com.common.message.controller.vo;

import lombok.Data;

/**
 * Runtime state of one RocketMQ listener container.
 */
@Data
public class RocketMQContainerStatusVO {
    private String beanName;
    private String consumerGroup;
    private String topic;
    private boolean running;
}
