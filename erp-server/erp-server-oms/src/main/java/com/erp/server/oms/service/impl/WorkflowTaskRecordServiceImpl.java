package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.WorkflowTaskRecordMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.KolSubB2cApplicationService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
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

    private static final String FORCE_RETRY_PERMISSION = "oms:workflowTaskRecord:forceRetry";


    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;

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
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,dto.getSourceTypeEnum().getName());
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
        // 批量保存所有实体。数据库唯一索引生效后，并发创建命中唯一冲突时让事务回滚，补偿任务会复用已提交记录。
        try {
            saveBatch(entities);
            return entities;
        } catch (DuplicateKeyException e) {
            log.warn("任务节点已存在，sourceType={}, sourceId={}", dto.getSourceTypeEnum().getCode(), dto.getSourceId(), e);
            throw new ServiceException(ApiError.WF_TASK_RECORD_DUPLICATE);
        }
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
        Map<String, List<WorkflowTaskRecordEntity>> map = list.stream()
                .collect(Collectors.groupingBy(e -> e.getSourceType() + ":" + e.getSourceId()));
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
            addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByCode(entity.getSourceType()));
            if (Objects.isNull(addTaskDTO.getSourceTypeEnum())) {
                log.warn("任务节点记录补偿重试失败，未知sourceType={}", entity.getSourceType());
                continue;
            }
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
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetry(WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        if (Objects.isNull(dto)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_REQUIRED);
        }
        checkForceRetryPermission();
        List<WorkflowTaskRecordEntity> taskList = listForceRetryTasks(dto);
        if (CollUtil.isEmpty(taskList)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        Map<String, List<WorkflowTaskRecordEntity>> taskGroup = taskList.stream()
                .collect(Collectors.groupingBy(e -> e.getSourceType() + ":" + e.getSourceId()));
        int resetCount = 0;
        int mqCount = 0;
        boolean retryByNodeId = CharSequenceUtil.isNotBlank(dto.getId());
        for (List<WorkflowTaskRecordEntity> groupTasks : taskGroup.values()) {
            List<WorkflowTaskRecordEntity> resetTasks = groupTasks.stream()
                    .filter(e -> !retryByNodeId || Objects.equals(e.getId(), dto.getId()))
                    .filter(this::allowForceRetry)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(resetTasks)) {
                continue;
            }
            for (WorkflowTaskRecordEntity entity : resetTasks) {
                resetForceRetryTask(entity, dto);
                resetCount++;
            }
            WorkflowTaskRecordEntity firstTask = groupTasks.stream()
                    .min(Comparator.comparing(WorkflowTaskRecordEntity::getIndex))
                    .orElse(resetTasks.get(0));
            registerForceRetryMq(firstTask);
            mqCount++;
        }
        if (resetCount == 0) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE);
        }
        addForceRetryLog(dto, taskList, resetCount, mqCount);

        WorkflowTaskRecordEntity first = taskList.get(0);
        WorkflowTaskRecordDTO.ForceRetryResultDTO resultDTO = new WorkflowTaskRecordDTO.ForceRetryResultDTO();
        resultDTO.setSourceType(first.getSourceType());
        resultDTO.setSourceId(first.getSourceId());
        resultDTO.setResetCount(resetCount);
        resultDTO.setScheduledMqCount(mqCount);
        resultDTO.setMqCount(mqCount);
        return resultDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public WorkflowTaskRecordEntity getActiveTask(String sourceId, String sourceType, Integer index) {
        if (CharSequenceUtil.isBlank(sourceId) || CharSequenceUtil.isBlank(sourceType) || Objects.isNull(index)) {
            return null;
        }
        return this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getSourceId, sourceId)
                .eq(WorkflowTaskRecordEntity::getSourceType, sourceType)
                .eq(WorkflowTaskRecordEntity::getIndex, index)
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .last("limit 1")
                .one();
    }

    @Override
    public Boolean claimTask(String id, String fromStatus) {
        if (CharSequenceUtil.isBlank(id) || CharSequenceUtil.isBlank(fromStatus)) {
            return Boolean.FALSE;
        }
        return this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, id)
                .eq(WorkflowTaskRecordEntity::getStatus, fromStatus)
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PROCESSING.getCode())
                .update();
    }

    private List<WorkflowTaskRecordEntity> listForceRetryTasks(WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        if (CharSequenceUtil.isNotBlank(dto.getId())) {
            WorkflowTaskRecordEntity entity = getById(dto.getId());
            return Objects.isNull(entity) ? Collections.emptyList() : listBySourceId(entity.getSourceId(), entity.getSourceType());
        }
        if (CharSequenceUtil.isBlank(dto.getSourceType()) || CharSequenceUtil.isBlank(dto.getSourceId())) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE);
        }
        return listBySourceId(dto.getSourceId(), dto.getSourceType());
    }

    private void resetForceRetryTask(WorkflowTaskRecordEntity entity, WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        String remark = appendForceRetryRemark(entity.getRemark(), dto.getRemark());
        this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, entity.getId())
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PENDING.getCode())
                .set(WorkflowTaskRecordEntity::getRetryCount, Optional.ofNullable(dto.getRetryCount()).orElse(0))
                .set(WorkflowTaskRecordEntity::getLastError, "")
                .set(WorkflowTaskRecordEntity::getRemark, remark)
                .update();
    }

    private boolean allowForceRetry(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getStatus(), WorkflowTaskRecordStatusEnum.SUCCESS.getCode())) {
            return false;
        }
        if (!Objects.equals(entity.getStatus(), WorkflowTaskRecordStatusEnum.PROCESSING.getCode())) {
            return true;
        }
        LocalDateTime updateTime = entity.getUpdateTime();
        return Objects.nonNull(updateTime) && updateTime.plusMinutes(3).isBefore(LocalDateTime.now());
    }

    private String appendForceRetryRemark(String oldRemark, String remark) {
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String current = StrUtil.format("人工强制重试，操作人：{}，备注：{}", operator, StrUtil.blankToDefault(remark, ""));
        if (CharSequenceUtil.isBlank(oldRemark)) {
            return current;
        }
        return oldRemark + "\n" + current;
    }

    private Boolean sendForceRetryMq(WorkflowTaskRecordEntity entity) {
        WorkflowTaskRecordTypeEnum sourceTypeEnum = WorkflowTaskRecordTypeEnum.getByCode(entity.getSourceType());
        if (Objects.isNull(sourceTypeEnum)) {
            log.warn("任务节点人工强制重试失败，未知sourceType={}", entity.getSourceType());
            return Boolean.FALSE;
        }
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
        addTaskDTO.setSourceId(entity.getSourceId());
        addTaskDTO.setSourceCode(entity.getSourceCode());
        addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
        addTaskDTO.setSourceTypeEnum(sourceTypeEnum);
        addTaskDTO.setTraceId(entity.getTraceId());
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, entity.getSourceId(), 1);
        if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_MQ_SEND_FAILED, JSONUtil.toJsonStr(result));
        }
        return Boolean.TRUE;
    }

    private void registerForceRetryMq(WorkflowTaskRecordEntity entity) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        sendForceRetryMq(entity);
                    } catch (Exception e) {
                        log.error("任务节点人工强制重试事务提交后发送MQ失败，sourceType={}, sourceId={}", entity.getSourceType(), entity.getSourceId(), e);
                    }
                }
            });
            return;
        }
        sendForceRetryMq(entity);
    }

    private void addForceRetryLog(WorkflowTaskRecordDTO.ForceRetryDTO dto, List<WorkflowTaskRecordEntity> taskList, int resetCount, int mqCount) {
        WorkflowTaskRecordEntity first = taskList.get(0);
        String content = StrUtil.format("用户【{}】人工强制重试任务节点，sourceType=【{}】，sourceId=【{}】，重置节点数=【{}】，调度MQ数=【{}】，备注=【{}】",
                UserContext.getDefaultLoginUser().getUserName(),
                first.getSourceType(),
                first.getSourceId(),
                resetCount,
                mqCount,
                StrUtil.blankToDefault(dto.getRemark(), ""));
        operateLogService.addModuleOperateLog(content, resolveModuleType(first), resolveBusinessId(first), "任务强制重试");
    }

    private void checkForceRetryPermission() {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(loginUser) && Boolean.TRUE.equals(loginUser.getIsSupper())) {
            return;
        }
        List<String> permissionList = Objects.isNull(loginUser) ? Collections.emptyList() : loginUser.getPermissionList();
        if (CollUtil.isNotEmpty(permissionList) && permissionList.contains(FORCE_RETRY_PERMISSION)) {
            return;
        }
        throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_FORBIDDEN);
    }

    private String resolveModuleType(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_APPLICATION_APPROVE.getCode())
                || Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_SUB_APPROVE.getCode())) {
            return ModuleTypeEnum.KOL_B2C_APPLICATION.getCode();
        }
        return null;
    }

    private String resolveBusinessId(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_SUB_APPROVE.getCode())) {
            KolSubB2cApplicationEntity subEntity = kolSubB2cApplicationService.getById(entity.getSourceId());
            if (Objects.nonNull(subEntity) && CharSequenceUtil.isNotBlank(subEntity.getSourceId())) {
                return subEntity.getSourceId();
            }
        }
        return entity.getSourceId();
    }
}
