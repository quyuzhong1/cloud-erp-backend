package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务审核通过
 *
 * @Classname TaskApprovalPass
 * @Description TODO
 * @Date 2022-10-19 11:38
 * @Created by yl
 */
public class TaskApprovalPass implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * 任务审核通过
     *
     * @param taskIds
     * @param state
     * @param userId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-19 11:39
     */
    @Override
    public Boolean updateTaskState(List<String> taskIds, String  productId, String userId) {
        //根据任务id 获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        // 只有带审核 和 完成待审核 的状态 才可以审核通过
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        Integer finishWaitConfirmCode = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        long count = list.stream().filter(p -> (p.getStatus() != waitConfirmCode || p.getStatus() != finishWaitConfirmCode)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
        //审核任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();
        //这个是审核任务
      //  List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        projectTaskService.updateTaskState(taskIdList, approvalIngCode, null, null);

        return true;
    }
}
