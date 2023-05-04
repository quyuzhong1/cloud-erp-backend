package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ProcessTaskManagementMapper;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment) {
        // 更新任务审批状态
        boolean update = lambdaUpdate()
                .set(ProcessTaskManagementEntity::getTaskStatus, ApproveTypeEnum.PASS.equals(approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT)
                .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                .set(StrUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                .eq(ProcessTaskManagementEntity::getId, taskId)
                .update();
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
                .collect(Collectors.groupingBy(ProcessTaskManagementEntity::getCurrentNodeId, LinkedHashMap::new, Collectors.toList()));
        return nodeMap;
    }
}
