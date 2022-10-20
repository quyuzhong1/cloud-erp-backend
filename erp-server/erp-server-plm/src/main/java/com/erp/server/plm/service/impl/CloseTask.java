package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 关闭任务
 *
 * @Classname CloseTask
 * @Description TODO
 * @Date 2022-10-18 18:27
 * @Created by yl
 */
public class CloseTask implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * 关闭任务
     *
     * @param taskIds
     * @param productId
     * @return
     */
    @Override
    public Boolean updateTaskState(List<String> taskIds, String productId,String userId) {
        //获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //进行中
        Integer ingCode = TaskStateEnum.ING.getCode();
        //待审核
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        long flag = list.stream().filter(t -> (ingCode.equals(t.getStatus())   || waitConfirmCode.equals(waitConfirmCode))).count();
        if (flag !=list.size()) {
            throw new ServiceException(ApiError.ERROR_95032);
        }
        //更改状态
        return projectTaskService.updateTaskState(taskIds, TaskStateEnum.CLOSE.getCode(), null, null);

    }
}
