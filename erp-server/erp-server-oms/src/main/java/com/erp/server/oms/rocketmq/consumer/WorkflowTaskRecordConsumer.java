package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.server.oms.orchestration.WorkflowTaskStepDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 任务编排单步调度消费者
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, selectorExpression = RocketMqNewTag.OMS_WORKFLOW_TASK_RECORD_TAG, consumerGroup = RocketMqConsumerGroup.OMS_WORKFLOW_TASK_RECORD)
public class WorkflowTaskRecordConsumer implements RocketMQListener<WorkflowTaskRecordDTO.AddTaskDTO> {

    @Resource
    private WorkflowTaskStepDispatcher workflowTaskStepDispatcher;

    /**
     * 接收单步调度 MQ，委托 {@link WorkflowTaskStepDispatcher#dispatch} 执行一个节点。
     */
    @Override
    public void onMessage(WorkflowTaskRecordDTO.AddTaskDTO mqDTO) {
        log.info("WorkflowTaskRecordConsumer接收到调度消息：{}", JSONUtil.toJsonStr(mqDTO));
        if (Objects.isNull(mqDTO)) {
            log.error("WorkflowTaskRecordConsumer接受参数为空");
            return;
        }
        workflowTaskStepDispatcher.dispatch(mqDTO);
    }
}
