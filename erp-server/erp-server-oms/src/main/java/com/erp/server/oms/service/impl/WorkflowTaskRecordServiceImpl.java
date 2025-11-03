package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.mapper.WorkflowTaskRecordMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * <p>
 * 任务节点记录表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
@Slf4j
@Service
public class WorkflowTaskRecordServiceImpl extends SuperServiceImpl<WorkflowTaskRecordMapper, WorkflowTaskRecordEntity> implements WorkflowTaskRecordService {


    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 添加工作流任务记录
     *
     * @param dto 添加任务的数据传输对象，包含字典类型、源ID和源类型等信息
     * @return 创建的工作流任务记录实体列表
     * @throws ServiceException 当指定类型的工作流任务节点字典不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        //查询字典表 type = workflowTaskNode
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByType(dto.getDictBasicTypeEnum().getType(),dto.getSourceTypeEnum().getCode());
        if(CollUtil.isEmpty(dictList)){
            throw new ServiceException(ApiError.NOT_EXIST,dto.getSourceTypeEnum().getName());
        }
        // 根据 sort 字段升序排序
        dictList = dictList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort))
                .collect(Collectors.toList());

        List<WorkflowTaskRecordEntity> entities = new ArrayList<>();

        // 一次遍历完成实体创建和 nextId 设置
        for (int i = 0; i < dictList.size(); i++) {
            DictBasicDTO.ViewDTO viewDTO = dictList.get(i);
            WorkflowTaskRecordEntity entity = new WorkflowTaskRecordEntity();
            String id = IdWorker.getIdStr();
            entity.setId(id);
            entity.setSourceId(dto.getSourceId());
            entity.setSourceType(dto.getSourceTypeEnum().getCode());
            entity.setSourceCode(dto.getSourceCode());
            entity.setClassPath(viewDTO.getValue());
            entity.setIndex(i);
            entity.setDictBasicId(viewDTO.getId());
            entity.setTraceId(dto.getTraceId());
            if (i == 0) {
                entity.setInputData(JSON.toJSONString(dto.getFirstNodeInputData()));
            }
            entities.add(entity);
        }
        // 批量保存所有实体
        saveBatch(entities);
        return entities;
    }


    @Override
    public List<WorkflowTaskRecordEntity> listErrorTask() {
        return baseMapper.listErrorTask("");
    }

    @Override
    public void WorkflowTaskRecordRetryJob(String id) {
        List<WorkflowTaskRecordEntity> list = baseMapper.listErrorTask(id);
        if (CollectionUtil.isEmpty(list)) {
            return ;
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
    }

    @Override
    public List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport() {
        return  baseMapper.getTaskErrorReport();
    }

    @Override
    public void removeBySourceIdAndSourceType(String sourceId, String sourceType) {
        if (CharSequenceUtil.isAllNotBlank(sourceId,sourceType)){
            this.lambdaUpdate().eq(WorkflowTaskRecordEntity::getSourceId,sourceId)
                    .eq(WorkflowTaskRecordEntity::getSourceType,sourceType)
                    .remove();
        }
    }

    @Override
    public List<WorkflowTaskRecordEntity> listBySourceId(String soId, String sourceType) {
        if (CharSequenceUtil.isBlank(soId)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(WorkflowTaskRecordEntity::getSourceId,soId)
                .eq(WorkflowTaskRecordEntity::getSourceType,sourceType)
                .list();
    }
}