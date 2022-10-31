package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskOperatorRecordEntity;
import com.erp.server.plm.mapper.TaskOperatorRecordMapper;
import com.erp.server.plm.service.TaskOperatorRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname TaskOperatorRecordServiceImpl
 * @Description TODO
 * @Date 2022-10-20 15:43
 * @Created by yl
 */
@Service
public class TaskOperatorRecordServiceImpl extends ServiceImpl<TaskOperatorRecordMapper, TaskOperatorRecordEntity> implements TaskOperatorRecordService {

    /**
     * 批量保存记录
     *
     * @param taskIds
     * @param beforeState
     * @param afterState
     * @param uid
     * @param userName
     * @param comment
     * @return void
     * @author yl
     * @date 2022-10-20 16:14
     */

    @Override
    public void batchSaveRecord(List<String> taskIds, Integer beforeState, Integer afterState, String uid, String userName, String comment) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            List<TaskOperatorRecordEntity> addList = new ArrayList<>();
            for (String taskId : taskIds) {
                TaskOperatorRecordEntity record = new TaskOperatorRecordEntity();
                record.setOperatorId(uid);
                record.setOperatorName(userName);
                record.setBeforeState(beforeState);
                record.setAfterState(afterState);
                record.setTaskId(taskId);
                addList.add(record);
            }
            this.saveBatch(addList);
        }
    }

    /**
     * 根据任务id 获取任务操作记录
     *
     * @param taskId
     * @return java.util.List<com.erp.model.plm.entity.TaskOperatorRecordEntity>
     * @author yl
     * @date 2022-10-20 17:07
     */
    @Override
    public List<TaskOperatorRecordEntity> getByTaskId(String taskId) {
        LambdaQueryWrapper<TaskOperatorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskOperatorRecordEntity::getTaskId, taskId);
        return this.list(queryWrapper);
    }

    @Override
    public void batchSaveTaskRecord(List<ProjectTaskEntity> taskList, Integer afterState, String uid, String userName, String s) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            List<TaskOperatorRecordEntity> saveList = new ArrayList<>(taskList.size());
            for (ProjectTaskEntity task : taskList) {
                TaskOperatorRecordEntity record = new TaskOperatorRecordEntity();
                record.setOperatorId(uid);
                record.setOperatorName(userName);
                record.setBeforeState(task.getStatus());
                record.setAfterState(afterState);
                record.setTaskId(task.getId());
                saveList.add(record);
            }
            this.saveBatch(saveList);
        }
    }
}
