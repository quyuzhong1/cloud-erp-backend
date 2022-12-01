package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskOperatorRecordEntity;

import java.util.List;

/**
 * @Classname TaskOperatorRecordServoce
 * @Description TODO
 * @Date 2022-10-20 15:43
 * @Created by yl
 */
public interface TaskOperatorRecordService  extends IService<TaskOperatorRecordEntity> {
    void batchSaveRecord(List<String> taskIds, Integer beforeState, Integer afterState, String uid, String userName, String comment);

    List<TaskOperatorRecordEntity> getByTaskId(String taskId);

    void batchSaveTaskRecord(List<ProjectTaskEntity> list, Integer code, String uid, String userName, String s);

    void addTaskOperator(String taskId, Integer beforeState, Integer afterState, String uid, String userName);
}
