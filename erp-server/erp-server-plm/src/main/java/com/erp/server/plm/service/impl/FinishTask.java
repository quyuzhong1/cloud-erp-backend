package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 完成任务
 *
 * @Classname FinishTask
 * @Description TODO
 * @Date 2022-10-18 18:43
 * @Created by yl
 */
public class FinishTask implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private PreTaskService preTaskService;


    @Override
    public Boolean updateTaskState(List<String> taskIds, Integer state) {
        //获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);

        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //评审任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();


        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        /**
         * 评审任务
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        //表示有 评审任务 则要 踢出去
        if (CollectionUtils.isNotEmpty(reviewList) && reviewList.size() > 0) {
            throw new ServiceException(ApiError.ERROR_95033);
        }
        //一般任务 没有流程
        List<ProjectTaskEntity> noProcessList = generalTasks.stream().filter(p -> StringUtils.isBlank(p.getProcessId())).collect(Collectors.toList());

        //一般任务 有流程
        List<ProjectTaskEntity> processList = generalTasks.stream().filter(p -> StringUtils.isBlank(p.getProcessId())).collect(Collectors.toList());
        //进行中
        Integer ingCode = TaskStateEnum.ING.getCode();
        //找出 没有流程中 不是进行中的任务 如果有表示 不能完成任务
        long noProcess = noProcessList.stream().filter(t -> t.getStatus() != ingCode).count();
        if (noProcess > 0) {
            throw new ServiceException(ApiError.ERROR_95034);
        }
        //其它的任务数量 前置任务 和子任务
        List<String> noProcessTaskIds = noProcessList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        //检查前置任务是否完成
        preTaskService.checkPreTaskFinish(noProcessTaskIds);
        //检查子任务是否有完成
        projectTaskService.checkSonTaskFinish(noProcessTaskIds);

        return null;
    }
}
