package com.erp.server.plm.service.impl;

import com.erp.server.plm.service.TaskOperateStrategy;

import java.util.List;

/**
 * 操作任务
 *
 * @Classname TaskOperate
 * @Description TODO
 * @Date 2022-10-19 14:58
 * @Created by yl
 */
public class TaskOperate {

    private TaskOperateStrategy strategy;

    public TaskOperate(TaskOperateStrategy strategy){
        this.strategy = strategy;
    }
    public Boolean updateTaskState(List<String> taskIds, String productId, String userId){
        return strategy.updateTaskState(taskIds,productId,userId);
    }
}
