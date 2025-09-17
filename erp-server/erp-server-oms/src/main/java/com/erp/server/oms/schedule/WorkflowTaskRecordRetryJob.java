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

    private static String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @Resource
    private MQProducerService mqProducerService;


    @Resource
    private DictBasicService dictBasicService;

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
            long count = list.stream().filter(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.FAILED.getCode()) && e.getRetryCount() > 3).count();
            if(count > 0){
                continue;
            }

            WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
            addTaskDTO.setSourceId(workflowTaskRecordEntities.get(0).getSourceId());
            addTaskDTO.setSourceCode(workflowTaskRecordEntities.get(0).getSourceCode());
            addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
            addTaskDTO.setSourceTypeEnum(SourceTypeEnum.EXHIBITION_ORDER);
            addTaskDTO.setTraceId(workflowTaskRecordEntities.get(0).getTraceId());
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, workflowTaskRecordEntities.get(0).getSourceId(),1);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RuntimeException(StrUtil.format("展会订单任务节点记录补偿重试MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }

        List<WorkflowTaskRecordEntity> retryList = list.stream()
                .filter(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.FAILED.getCode()) && e.getRetryCount() > 3)
                .sorted(Comparator.comparing(WorkflowTaskRecordEntity::getSourceType))
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(retryList)){
            //查询字典表 type = workflowTaskNode
            List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.WORKFLOW_TASK_NODE.getType());
            Map<String, String> dictMap = dictList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getId, DictBasicDTO.ViewDTO::getName));

            String url = "https://open.feishu.cn/open-apis/bot/v2/hook/b18f5837-19c7-4383-808b-3b9d34b591f3";
            String content = "任务节点：{}，任务类型：{}，任务单号：{}，重试次数：{}，最后错误信息：{}";
            for (WorkflowTaskRecordEntity entity : retryList) {
                StringBuffer sb = new StringBuffer();
                sb.append("任务节点记录补偿重试");
                sb.append("\n");
                String msg = StrUtil.format(content, SourceTypeEnum.getName(entity.getSourceCode()), entity.getSourceCode(), dictMap.getOrDefault(entity.getDictBasicId(), ""), entity.getRetryCount(), entity.getLastError());
                sb.append(msg);
                Map<String, Object> bodyMap = new HashMap<String, Object>();
                bodyMap.put("msg_type", "text");
                Map<String, String> contentMap = new HashMap<String, String>();
                contentMap.put("text", sb.toString());
                bodyMap.put("content", contentMap);
                XxlJobHelper.log("WorkflowTaskRecordRetryJob 发送至机器人");
                HttpUtil.post(url, JSON.toJSONString(bodyMap));
            }
        }
        XxlJobHelper.log("WorkflowTaskRecordRetryJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
