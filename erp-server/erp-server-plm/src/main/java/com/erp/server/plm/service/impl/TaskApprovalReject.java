package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核不通过
 *
 * @Classname TaskApprovalNoPass
 * @Description TODO
 * @Date 2022-10-19 14:19
 * @Created by yl
 */
public class TaskApprovalReject implements TaskOperateStrategy {


    @Autowired
    private ProjectTaskService projectTaskService;

    @Override
    public Boolean updateTaskState(List<String> taskIds, String  productId, String userId) {
        //审核不通过表示要博回的
        //根据任务id 获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();
        long count = list.stream().filter(t -> t.getStatus() != approvalIngCode).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95038);
        }
        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        boolean flag = projectTaskService.updateTaskState(taskIdList, TaskStateEnum.APPROVAL_NO_PASS.getCode(), null, null);
        return flag;
    }
}
