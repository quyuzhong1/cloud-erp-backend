package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.json.XMLTokener.entity;

/**
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, selectorExpression = "oms_workflow_task_record_tag", consumerGroup = RocketMqConsumerGroup.OMS_WORKFLOW_TASK_RECORD)
public class WorkflowTaskRecordConsumer implements RocketMQListener<WorkflowTaskRecordDTO.AddTaskDTO> {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public void onMessage(WorkflowTaskRecordDTO.AddTaskDTO mqDTO) {
        log.info("WorkflowTaskRecordConsumer接受到参数：mqDTO={}", JSONUtil.toJsonStr(mqDTO));
        //校验mqDTO不能为空
        if (Objects.isNull(mqDTO)) {
            log.error("WorkflowTaskRecordConsumer接受参数为空");
            return;
        }

        MDC.put("traceId", mqDTO.getTraceId());

        List<WorkflowTaskRecordEntity> list = workflowTaskRecordService.lambdaQuery().eq(WorkflowTaskRecordEntity::getSourceId, mqDTO.getSourceId()).orderByAsc(WorkflowTaskRecordEntity::getIndex).list();
        if(CollUtil.isEmpty(list)){
            log.error("根据sourceId未查询到任务记录，sourceId={}", mqDTO.getSourceId());
            return;
        }

        boolean allMatch = list.stream().allMatch(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.SUCCESS.getCode()));
        if(allMatch){
            log.error("所有任务节点均已成功，无需重复执行，sourceId={}", mqDTO.getSourceId());
            return;
        }

        //默认第一个节点的入参
        Map<Integer, WorkflowTaskRecordEntity> map = list.stream().collect(Collectors.toMap(WorkflowTaskRecordEntity::getIndex, Function.identity()));

        for (int i = 0; i < map.size(); i++) {
            WorkflowTaskRecordEntity entity = map.getOrDefault(i,null);
            if (Objects.isNull(entity)) {
                log.error("任务节点缺失，index={}, sourceId={}", i, mqDTO.getSourceId());
                break;
            }
            Boolean success = Boolean.FALSE;
            String status = entity.getStatus();
            WorkflowTaskRecordStatusEnum byCode = WorkflowTaskRecordStatusEnum.getByCode(status);
            switch (byCode) {
                case SUCCESS:
                    success = Boolean.TRUE;
                    break;
                case PROCESSING:
                    LocalDateTime updateTime = entity.getUpdateTime();
                    if (updateTime.plusMinutes(3).isAfter(LocalDateTime.now())) {
                        log.warn("节点处理中且未超时，index={}, sourceId={}", i, mqDTO.getSourceId());
                        break;
                    } else {
                        log.warn("节点处理超时，触发远程调用，index={}, sourceId={}", i, mqDTO.getSourceId());
                        success = remoteInvoke(map, entity, i, 1); // 超时重试
                    }
                    break;
                case PENDING:
                    success = remoteInvoke(map, entity, i, 0); // 初始执行
                    break;
                case FAILED:
                    success = remoteInvoke(map, entity, i, 1); // 失败重试
                    break;
                default:
                    log.error("未知状态码，index={}, status={}, sourceId={}", i, status, mqDTO.getSourceId());
                    break;
            }
            if(!success){
                log.error("远程调用失败，终止后续节点执行，index={}, sourceId={}", i, mqDTO.getSourceId());
                break;
            }
        }
    }

    private Boolean remoteInvoke(Map<Integer, WorkflowTaskRecordEntity> map,WorkflowTaskRecordEntity entity,Integer i,Integer plus) {
        String traceId = MDC.get("traceId");

        WorkflowTaskRecordEntity nextEntity = null;
        if( i != map.size() - 1){
            nextEntity = map.get(i + 1);
        }
        String classPath = entity.getClassPath();
        if(StringUtils.isBlank(classPath)){
            log.error("classPath is blank for workflowTaskRecordEntity id: {}", entity.getId());
            markAsFailed(entity,"classpath为空",plus);
            return Boolean.FALSE;
        }

        String jsonStr = entity.getInputData();
        if(StringUtils.isBlank(jsonStr)){
            log.error("inputData is blank for workflowTaskRecordEntity id: {}", entity.getId());
            String msg = StrUtil.format("traceId: 【{}】，inputData为空",traceId);
            markAsFailed(entity,msg,plus);
            return Boolean.FALSE;
        }

        //更新为执行中
        entity.setStatus(WorkflowTaskRecordStatusEnum.PROCESSING.getCode());
        workflowTaskRecordService.updateById(entity);

        //inputData是一个jsonStr 需要转换成Map
        Map<String, Object> inputDataMap;
        try {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            inputDataMap = gson.fromJson(jsonStr, mapType);
        } catch (Exception e) {
            log.error("Failed to parse inputData JSON for workflowTaskRecordEntity id: {}", entity.getId(), e);
            String msg = StrUtil.format("traceId: 【{}】，Failed to parse inputData JSON for workflowTaskRecordEntity id: 【{}】，e :{}",traceId,entity.getId(),e);
            markAsFailed(entity,msg,plus);
            return Boolean.FALSE;
        }

        String[] split = classPath.split("#");
        if (split.length != 2) {
            log.error("Invalid classPath format: {} for workflowTaskRecordEntity id: {}", classPath, entity.getId());
            String msg = StrUtil.format("traceId: 【{}】，Invalid classPath format: {} for workflowTaskRecordEntity id: {}",traceId,classPath, entity.getId());
            markAsFailed(entity,msg,plus);
            return Boolean.FALSE;
        }

        String controller = split[0];
        String methodName = split[1];

        WorkflowTaskRecordDTO.MqRequestDTO dto = new WorkflowTaskRecordDTO.MqRequestDTO();
        dto.setData(inputDataMap);

        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO;
        try {
            mqResponseDTO = FeignQuery.invoke(WorkflowTaskRecordDTO.MqResponseDTO.class, controller, methodName, Arrays.asList(dto));
        } catch (Exception e) {
            log.error("Feign invoke failed for workflowTaskRecordEntity id: {}", entity.getId(), e);
            String msg = StrUtil.format("traceId: 【{}】，Feign invoke failed for workflowTaskRecordEntity id: {}，e :{}",traceId, entity.getId(),e);
            markAsFailed(entity,msg,plus);
            return Boolean.FALSE;
        }

        if(Objects.isNull(mqResponseDTO)){
            entity.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
            entity.setRetryCount(entity.getRetryCount() + plus);
            workflowTaskRecordService.updateById(entity);
            return Boolean.FALSE;
        }else {
            entity.setOutputData(JSON.toJSONString(mqResponseDTO.getData()));

            String errorMsg = mqResponseDTO.getErrorMsg();
            if(StringUtils.isNotBlank(errorMsg)){
                entity.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
                entity.setLastError(errorMsg);
            }else {
                entity.setStatus(WorkflowTaskRecordStatusEnum.SUCCESS.getCode());
                entity.setLastError("");
                if(Objects.nonNull(nextEntity)){
                    nextEntity.setInputData(JSON.toJSONString(mqResponseDTO.getData()));
                    map.put(nextEntity.getIndex() ,nextEntity);
                    workflowTaskRecordService.updateById(nextEntity);
                }
            }
            entity.setRetryCount(entity.getRetryCount() + plus);
            workflowTaskRecordService.updateById(entity);
            return Boolean.TRUE;
        }
    }

    private void markAsFailed(WorkflowTaskRecordEntity entity,String errorMsg,Integer plus) {
        entity.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
        entity.setLastError(errorMsg);
        entity.setRetryCount(entity.getRetryCount() + plus);
        workflowTaskRecordService.updateById(entity);
    }
}

