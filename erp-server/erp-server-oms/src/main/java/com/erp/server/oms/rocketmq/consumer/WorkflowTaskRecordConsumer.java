package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
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
//        com.erp.server.sys.service.impl.ThirdNoticePushRecordServiceImpl.getUserList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
        //校验mqDTO不能为空
        if (Objects.isNull(mqDTO)) {
            log.error("WorkflowTaskRecordConsumer接受参数为空");
            return;
        }

        List<WorkflowTaskRecordEntity> list = workflowTaskRecordService.lambdaQuery().eq(WorkflowTaskRecordEntity::getSourceId, mqDTO.getSourceId()).orderByAsc(WorkflowTaskRecordEntity::getIndex).list();
        if(CollUtil.isEmpty(list)){
            log.error("");
            return;
        }

        boolean allMatch = list.stream().allMatch(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.SUCCESS.getCode()));
        if(allMatch){
            log.error("");
            return;
        }

        //默认第一个节点的入参
        Map<Integer, WorkflowTaskRecordEntity> map = list.stream().collect(Collectors.toMap(WorkflowTaskRecordEntity::getIndex, Function.identity()));

        for (int i = 0; i < map.size(); i++) {
            WorkflowTaskRecordEntity entity = map.get(i);
            Boolean success = Boolean.FALSE;
            String status = entity.getStatus();
            if(Objects.equals(status, WorkflowTaskRecordStatusEnum.SUCCESS.getCode())){
                success = Boolean.TRUE;
            }else  if(Objects.equals(status, WorkflowTaskRecordStatusEnum.PROCESSING.getCode())){
                LocalDateTime updateTime = entity.getUpdateTime();
                //判断updateTime 和当前时间是否不超过3分钟，如果是则break;
                if(updateTime.plusMinutes(3).isAfter(LocalDateTime.now())){
                    // 超时处理
                    success = remoteInvoke(map,entity,i,0);
                }
            }else  if(Objects.equals(status, WorkflowTaskRecordStatusEnum.PENDING.getCode())){
                success = remoteInvoke(map,entity,i,0);
            }else if(Objects.equals(status, WorkflowTaskRecordStatusEnum.FAILED.getCode())){
                success = remoteInvoke(map,entity,i,1);
            }
            if(!success){
                break;
            }
        }
    }

    private Boolean remoteInvoke(Map<Integer, WorkflowTaskRecordEntity> map,WorkflowTaskRecordEntity entity,Integer i,Integer plus) {
        WorkflowTaskRecordEntity nextEntity = null;
        if( i != map.size() - 1){
            nextEntity = map.get(i + 1);
        }
        String classPath = entity.getClassPath();
        if(StringUtils.isBlank(classPath)){
            log.error("classPath is blank for workflowTaskRecordEntity id: {}", entity.getId());
            markAsFailed(entity,"classpath为空");
            return Boolean.FALSE;
        }

        String jsonStr = entity.getInputData();
        if(StringUtils.isBlank(jsonStr)){
            log.error("inputData is blank for workflowTaskRecordEntity id: {}", entity.getId());
            markAsFailed(entity,"inputData为空");
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
            markAsFailed(entity,"inputData转json失败");
            return Boolean.FALSE;
        }

        String[] split = classPath.split("#");
        if (split.length != 2) {
            log.error("Invalid classPath format: {} for workflowTaskRecordEntity id: {}", classPath, entity.getId());
            markAsFailed(entity,"classPath格式异常");
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
            markAsFailed(entity,e.getMessage());
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

    private void markAsFailed(WorkflowTaskRecordEntity entity,String errorMsg) {
        entity.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
        entity.setLastError(errorMsg);
        workflowTaskRecordService.updateById(entity);
    }
}

