package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务节点记录表补偿重试
 */
@Component
@Slf4j
public class WorkflowTaskRecordRetryJob {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @Resource
    private MQProducerService mqProducerService;


    /**
     * 任务节点记录表补偿重试
     * @Author jack
     **/
    @XxlJob("WorkflowTaskRecordRetryJob")
    public ReturnT<String> WorkflowTaskRecordRetryJob() {
        XxlJobHelper.log("WorkflowTaskRecordRetryJob 执行开始");

        List<WorkflowTaskRecordEntity> list = workflowTaskRecordService.listErrorTask();
        if (CollectionUtil.isEmpty(list)) {
            return ReturnT.SUCCESS;
        }

        Map<String, List<WorkflowTaskRecordEntity>> map = list.stream().collect(Collectors.groupingBy(WorkflowTaskRecordEntity::getSourceId));
        for (Map.Entry<String, List<WorkflowTaskRecordEntity>> entry : map.entrySet()) {
            List<WorkflowTaskRecordEntity> workflowTaskRecordEntities = entry.getValue();
            if(CollUtil.isEmpty(workflowTaskRecordEntities)){
                continue;
            }
            long count = workflowTaskRecordEntities.stream().filter(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.FAILED.getCode()) && e.getRetryCount() > 3).count();
            if(count > 0){
                continue;
            }
            WorkflowTaskRecordEntity entity = workflowTaskRecordEntities.get(0);
            WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
            addTaskDTO.setSourceId(entity.getSourceId());
            addTaskDTO.setSourceCode(entity.getSourceCode());
            addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
            addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByName(entity.getSourceType()));
            addTaskDTO.setTraceId(entity.getTraceId());
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, workflowTaskRecordEntities.get(0).getSourceId(),1);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                XxlJobHelper.log(StrUtil.format("展会订单任务节点记录补偿重试MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
        XxlJobHelper.log("WorkflowTaskRecordRetryJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
