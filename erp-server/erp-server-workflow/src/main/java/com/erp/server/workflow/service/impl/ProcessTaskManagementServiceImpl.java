package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ProcessTaskManagementMapper;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Override
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment, String activityId) {
        ProcessTaskManagementEntity entity = getById(taskId);
        boolean update;
        if(ApproveTypeEnum.REJECT_APPOINT.equals(approveType)) {
            // 驳回到指定节点
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(StrUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurrentApproveId())
                    .eq(ProcessTaskManagementEntity::getProcessInstanceId, entity.getProcessInstanceId())
                    .ne(ProcessTaskManagementEntity::getCurrentActivityId, activityId)
                    .update();
        }else {
            // 更新任务审批状态
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveTypeEnum.PASS.equals(approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(StrUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurrentApproveId())
                    .eq(ProcessTaskManagementEntity::getExecutionId, entity.getExecutionId())
                    .eq(ProcessTaskManagementEntity::getTaskId, entity.getTaskId())
                    .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE_ING)
                    .eq(ProcessTaskManagementEntity::getCurrentActivityId, entity.getCurrentActivityId())
                    .update();
        }
        if (!update) {
            throw new RuntimeException("更新任务审批状态失败");
        }
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
        if(CollectionUtil.isEmpty(list)){
            return new LinkedHashMap<>();
        }
        LinkedHashMap<String, List<ProcessTaskManagementEntity>> nodeMap = list.stream()
                .collect(Collectors.groupingBy(ProcessTaskManagementEntity::getCurrentActivityId, LinkedHashMap::new, Collectors.toList()));
        return nodeMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProcessTask(ProcessTaskManagementEntity insertTask) {
        // 赋值上级节点id
        ProcessTaskManagementEntity entity = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, insertTask.getProcessInstanceId())
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                .ne(ProcessTaskManagementEntity::getCurrentActivityId, insertTask.getCurrentActivityId())
                .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                .last("limit 1")
                .one();
        if(null != entity){
            insertTask.setPreActivityId(entity.getCurrentActivityId());
        }
        boolean save = save(insertTask);
        if (!save) {
            throw new RuntimeException("保存流程任务失败");
        }
    }
}
