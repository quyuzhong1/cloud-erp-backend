package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 开始任务
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 15:57
 * @Created by yl
 */
public class StartTask implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * 开始任务
     *
     * @param taskIds
     * @param state
     * @return
     */
    @Override
    @Transactional
    public Boolean updateTaskState(List<String> taskIds, Integer state) {
        //获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //审核任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(generalTasks)) {
            List<String> taskIdList = generalTasks.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            projectTaskService.updateTaskState(taskIdList, TaskStateEnum.ING.getCode());
        }

        /**
         * 审核任务要 启动流程 任务评审流程
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(reviewList)){


        }
        return null;
    }
}
