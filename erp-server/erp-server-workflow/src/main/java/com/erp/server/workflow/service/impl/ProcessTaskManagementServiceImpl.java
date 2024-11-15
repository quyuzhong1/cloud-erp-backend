package com.erp.server.workflow.service.impl;

import com.common.core.constant.SqlConstants;
import org.apache.commons.collections4.CollectionUtils;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementAttachmentDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementAttachmentEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.TimeoutStatusEnum;
import com.erp.server.workflow.mapper.ProcessTaskManagementMapper;
import com.erp.server.workflow.service.ProcessTaskCcService;
import com.erp.server.workflow.service.ProcessTaskManagementAttachmentService;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessTaskManagementServiceImpl extends SuperServiceImpl<ProcessTaskManagementMapper, ProcessTaskManagementEntity> implements ProcessTaskManagementService {

    @Resource
    private ProcessTaskCcService processTaskCcService;
    @Resource
    private ProcessTaskManagementAttachmentService attachmentService;

    @Override
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment, String activityId, ProcessManagementEntity managementEntity, @Nullable Map<String, Object> variablesMap) {
        ProcessTaskManagementEntity entity = getById(taskId);
        boolean update;
        if(ApproveTypeEnum.REJECT_APPOINT.equals(approveType)) {
            // 驳回到指定节点
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(CharSequenceUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getApproveName, entity.getCurApproveName())
                    .eq(ProcessTaskManagementEntity::getProcessInstanceId, entity.getProcessInstanceId())
                    .ne(ProcessTaskManagementEntity::getCurActivityId, activityId)
                    .update();
        }else {
            // 更新任务审批状态
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveTypeEnum.PASS.equals(approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(CharSequenceUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getApproveName, entity.getCurApproveName())
                    .eq(ProcessTaskManagementEntity::getExecutionId, entity.getExecutionId())
                    .eq(ProcessTaskManagementEntity::getTaskId, entity.getTaskId())
                    .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE_ING)
                    .eq(ProcessTaskManagementEntity::getCurActivityId, entity.getCurActivityId())
                    .update();

            // 保存附件
            if(variablesMap != null){
                List<ProcessTaskManagementAttachmentDTO.CommonDTO> attachmentList = JSON.parseArray(JSON.toJSONString(variablesMap.get("attachmentList")), ProcessTaskManagementAttachmentDTO.CommonDTO.class);
                if(attachmentList != null && !attachmentList.isEmpty()){
                    List<ProcessTaskManagementAttachmentEntity> saveList = new ArrayList<>(attachmentList.size());
                    for (ProcessTaskManagementAttachmentDTO.CommonDTO attachmentDTO : attachmentList) {
                        ProcessTaskManagementAttachmentEntity entity1 = new ProcessTaskManagementAttachmentEntity();
                        entity1.setAttachName(attachmentDTO.getAttachName());
                        entity1.setAttachUrl(attachmentDTO.getAttachUrl());
                        entity1.setMainId(entity.getId());
                        saveList.add(entity1);
                    }
                    attachmentService.saveBatch(saveList);
                }
            }
        }
        // 发送抄送消息
        String title = CharSequenceUtil.format("【流程管理中心】审批结果抄送");
        String content = CharSequenceUtil.format("**单据名称: **{}\n**审批人：** {} \n**审批结果：**{}！", managementEntity.getProcessName(), entity.getCurApproveName(), approveType.getName());
        processTaskCcService.sendCcMsg(entity, title, content);
        return Boolean.TRUE;
    }

    @Override
    public LinkedHashMap<String, List<ProcessTaskManagementEntity>> listHisByProcessInstanceId(String processInstanceId, Integer num) {
        // 查询所有历史任务
        List<ProcessTaskManagementEntity> list = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId)
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                .list();
        if(CollectionUtils.isEmpty(list)){
            return new LinkedHashMap<>();
        }
        return list.stream()
                .collect(Collectors.groupingBy(ProcessTaskManagementEntity::getCurActivityId, LinkedHashMap::new, Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessTaskManagementEntity saveProcessTask(ProcessTaskManagementEntity insertTask) {
        // 赋值上级节点id
        ProcessTaskManagementEntity entity = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, insertTask.getProcessInstanceId())
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                .ne(ProcessTaskManagementEntity::getCurActivityId, insertTask.getCurActivityId())
                .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                .last(SqlConstants.LIMIT_1)
                .one();
        if(null != entity){
            insertTask.setPreActivityId(entity.getCurActivityId());
        }
        boolean save = save(insertTask);
        if (!save) {
            throw new RuntimeException("保存流程任务失败");
        }
        return insertTask;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransfer(String taskId, String targetUserId, String targetUserName, String sourceUserId, String remark) {
        List<ProcessTaskManagementEntity> entityList = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getTaskId, taskId)
                .eq(CharSequenceUtil.isNotBlank(sourceUserId), ProcessTaskManagementEntity::getCurApproveId, sourceUserId)
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE_ING)
                .list();
        if(CollectionUtils.isEmpty(entityList)){
            throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
        }
        // 关闭原有记录
        for (ProcessTaskManagementEntity entity : entityList) {
            boolean update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getRemark, CharSequenceUtil.format("【{}】已将任务转移给【{}】办理，备注：{}", entity.getCurApproveName(), targetUserName, remark))
                    .eq(ProcessTaskManagementEntity::getId, entity.getId())
                    .update();
        }
        // 新增审批记录
        ProcessTaskManagementEntity insertEntity = ProcessTaskManagementEntity.getByEntity(entityList.get(0), targetUserId, targetUserName);
        boolean save = save(insertEntity);
        if (!save) {
            throw new RuntimeException(" updateTransfer 任务转办 保存流程任务失败");
        }
        // 转移抄送关联数据
        processTaskCcService.updateCcTransfer(entityList, insertEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstanceId(String processInstanceId) {
        boolean remove = lambdaUpdate()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId)
                .remove();
        if (!remove) {
            throw new RuntimeException("删除流程任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTimeoutStatus(List<String> taskManagementIds, TimeoutStatusEnum timeoutStatusEnum) {
        boolean update = lambdaUpdate()
                .set(ProcessTaskManagementEntity::getTimeoutStatus, timeoutStatusEnum)
                .in(ProcessTaskManagementEntity::getId, taskManagementIds)
                .update();
        if (!update) {
            throw new RuntimeException("更新任务超时状态失败");
        }
    }

    @Override
    public ProcessTaskManagementEntity lastTask(String processInstanceId) {
        ProcessTaskManagementEntity entity = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId)
                .in(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE, ApproveStatusEnum.REJECT)
                .orderByDesc(ProcessTaskManagementEntity::getApproveTime, ProcessTaskManagementEntity::getTaskStatus)
                .last(SqlConstants.LIMIT_1)
                .oneOpt().orElseThrow(() -> new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS));
        return entity;
    }

    /**
     * 根据业务id获取流程实例信息
     * @Author Luo_WG
     * @Date 2023/7/4 19:37
     * @param businessIds
     * @return com.erp.model.workflow.entity.ProcessManagementEntity
     **/
    @Override
    public List<ProcessTaskManagementEntity> listProcessByBusinessId(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listByProcessInstanceId(businessIds);
    }

    @Override
    public List<ProcessTaskManagementEntity> listPreActivityTask(String taskManagementId, String processInstanceId) {
        return baseMapper.listPreActivityTask(taskManagementId, processInstanceId);
    }

    @Override
    public List<ProcessTaskManagementDTO.ApproveHistoryDTO> listApproveHistory(String businessId) {
        if(StringUtils.isBlank(businessId)){
            return Collections.emptyList();
        }
        List<ProcessTaskManagementDTO.ApproveHistoryDTO> resultList = baseMapper.listApproveHistory(businessId);
        if(resultList.isEmpty()){
            return Collections.emptyList();
        }
        List<String> taskIds = resultList.stream().map(item -> item.getTaskId()).distinct().collect(Collectors.toList());
        List<ProcessTaskManagementAttachmentEntity> attachmentEntityList = attachmentService.lambdaQuery().in(ProcessTaskManagementAttachmentEntity::getMainId, taskIds).list();
        Map<String, List<ProcessTaskManagementAttachmentEntity>> groupByTaskId = attachmentEntityList.stream().collect(Collectors.groupingBy(item -> item.getMainId()));
        for (ProcessTaskManagementDTO.ApproveHistoryDTO taskDTO : resultList) {
            List<ProcessTaskManagementAttachmentEntity> attachmentList = groupByTaskId.get(taskDTO.getTaskId());
            if(attachmentList != null && !attachmentList.isEmpty()) {
                List<ProcessTaskManagementAttachmentDTO.CommonDTO> attachCommonDTOList = new ArrayList<>();
                attachmentList.forEach(item -> attachCommonDTOList.add(new ProcessTaskManagementAttachmentDTO.CommonDTO(item.getAttachUrl(), item.getAttachName())));
                taskDTO.setAttachmentList(attachCommonDTOList);
            }
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskManagementEntity> listProcessByBusinessKey(ProcessManagementDTO.TaskKeyInfoDTO dto) {
        return baseMapper.listProcessByBusinessKey(dto);
    }

}
