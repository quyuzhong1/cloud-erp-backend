package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;
import com.erp.server.plm.mapper.TaskChargeDistributionMapper;
import com.erp.server.plm.service.TaskChargeDistributionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/29 14:37
 */
@Service
public class TaskChargeDistributionServiceImpl extends ServiceImpl<TaskChargeDistributionMapper, TaskChargeDistributionEntity> implements TaskChargeDistributionService {

    @Override
    public List<TaskChargeDistributionEntity> listBySourceAndTaskId(Integer source, String taskId) {
        LambdaQueryWrapper<TaskChargeDistributionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskChargeDistributionEntity::getSource,source);
        queryWrapper.eq(TaskChargeDistributionEntity::getTaskId,taskId);
        queryWrapper.orderByAsc(TaskChargeDistributionEntity::getSeq);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public void removeAndSave(String taskId, List<TaskChargeDistributionEntity> approvalList, Integer source) {
        //删除
        removeBySourceAndTaskId(source,taskId);
        //新增
        if (CollectionUtils.isNotEmpty(approvalList)) {
            Integer seq = MathUtil.ONE;
           for (TaskChargeDistributionEntity obj:approvalList) {
                obj.setTaskId(taskId);
                obj.setSource(source);
                obj.setSeq(seq);
                seq++;
            };
           this.saveBatch(approvalList);
        }
    }

    @Override
    public void removeBySourceAndTaskId(Integer source, String taskId) {
        LambdaUpdateWrapper<TaskChargeDistributionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(TaskChargeDistributionEntity::getTaskId,taskId);
        updateWrapper.set(TaskChargeDistributionEntity::getSource,source);
        //删除
        this.remove(updateWrapper);
    }

}
