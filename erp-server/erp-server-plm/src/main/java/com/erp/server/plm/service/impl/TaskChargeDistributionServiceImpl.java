package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;
import com.erp.server.plm.mapper.TaskChargeDistributionMapper;
import com.erp.server.plm.service.TaskChargeDistributionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        queryWrapper.eq(TaskChargeDistributionEntity::getSource, source);
        queryWrapper.eq(TaskChargeDistributionEntity::getTaskId, taskId);
        queryWrapper.orderByAsc(TaskChargeDistributionEntity::getSeq);
        return this.list(queryWrapper);
    }

    /**
     * 根据来源和任务id 集合 获取到对应数据
     *
     * @param source
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskChargeDistributionEntity>
     * @author yl
     * @date 2023-02-28 14:31
     */
    @Override
    public List<TaskChargeDistributionEntity> listBySourceAndTaskIdList(Integer source, List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TaskChargeDistributionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskChargeDistributionEntity::getSource, source);
        queryWrapper.in(TaskChargeDistributionEntity::getTaskId, taskIds);
        queryWrapper.orderByAsc(TaskChargeDistributionEntity::getSeq);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAndSave(String taskId, List<TaskChargeDistributionEntity> approvalList, Integer source) {
        //删除
        removeBySourceAndTaskId(source, taskId);
        //新增
        if (CollectionUtils.isNotEmpty(approvalList)) {
            Integer seq = MathUtil.ONE;
            for (TaskChargeDistributionEntity obj : approvalList) {
                obj.setTaskId(taskId);
                obj.setSource(source);
                obj.setSeq(seq);
                obj.setId(null);
                seq++;
            }

            this.saveBatch(approvalList);
        }
    }

    @Override
    public void removeBySourceAndTaskId(Integer source, String taskId) {
        LambdaUpdateWrapper<TaskChargeDistributionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskChargeDistributionEntity::getTaskId, taskId);
        updateWrapper.eq(TaskChargeDistributionEntity::getSource, source);
        //删除
        this.remove(updateWrapper);
    }

    @Override
    public List<TaskChargeDistributionEntity> listBySourceAndRoleName(List<Integer> source, String name) {
        return this.baseMapper.listBySourceAndRoleName(source, name);
    }


    /**
     * 产品另存为模板 同步任务审核人
     *
     * @param taskSourceList
     * @return void
     * @author yl
     * @date 2023-03-09 20:15
     */
    @Override
    public void syncTemplateTaskChargeDistribution(List<CopySourceDTO> taskSourceList) {
        if (CollectionUtils.isEmpty(taskSourceList)) {
            return;
        }
        List<String> taskIds = taskSourceList.stream().map(CopySourceDTO::getDataId).collect(Collectors.toList());
        List<TaskChargeDistributionEntity> list = getByTaskIds(taskIds);
        //需要保存的
        List<TaskChargeDistributionEntity> addList = new ArrayList<>(list.size());
        for (TaskChargeDistributionEntity item : list) {
            String newTaskId = taskSourceList.stream().filter(t -> t.getDataId().equals(item.getTaskId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");

            if (StringUtils.isNotBlank(newTaskId)) {
                TaskChargeDistributionEntity addEntity = new TaskChargeDistributionEntity();
                BeanMapper.copy(item, addEntity);
                addEntity.setId(IdWorker.getIdStr());
                addEntity.setTaskId(newTaskId);
                addEntity.setSource(MathUtil.TWO);
                addList.add(addEntity);
            }
        }
        this.saveBatch(addList);
    }


    /**
     * 根据任务id 集合获取任务
     *
     * @param taskIds
     * @return
     */
    private List<TaskChargeDistributionEntity> getByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TaskChargeDistributionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TaskChargeDistributionEntity::getTaskId, taskIds);
        queryWrapper.orderByAsc(TaskChargeDistributionEntity::getSeq);
        return this.list(queryWrapper);
    }

}
