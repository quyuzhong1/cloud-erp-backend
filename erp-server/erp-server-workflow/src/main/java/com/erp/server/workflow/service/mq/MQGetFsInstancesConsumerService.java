package com.erp.server.workflow.service.mq;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.workflow.service.FsInstancesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 飞书获取单个审批实例详情
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FS_INSTANCES_TO_WORKFLOW_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FS_INSTANCES_TO_WORKFLOW_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FS_INSTANCES_TO_WORKFLOW_GROUP)
public class MQGetFsInstancesConsumerService  extends AbstractNewPlatformConsumerHandler {

    @Resource
    private FsInstancesService fsInstancesService;

    @Override
    public String getBizName() {
        return "飞书获取单个审批实例详情";
    }

    @Override
    public void handle(String data) {
        fsInstancesService.pullFsInstancesDetails(data);
    }

}