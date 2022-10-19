package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 发布任务
 *
 * @Classname ReleaseTask
 * @Description TODO
 * @Date 2022-10-18 15:18
 * @Created by yl
 */
@Service
public class ReleaseTask implements TaskOperateStrategy {


    @Autowired
    private ProjectTaskService projectTaskService;


    /**
     * 更改任务状态为 1  前提是检查任务状态应该为待发布0
     *
     * @param taskIds
     * @param state
     * @return
     */
    @Override
    public Boolean updateTaskState(List<String> taskIds, Integer state,String userId) {
        //待发布
        Integer releasedCode = TaskStateEnum.TO_BE_RELEASED.getCode();
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //统计项目状态为  不是待发布的任务
        long releasedCount = list.stream().filter(t -> !releasedCode.equals(t.getStatus())).count();
        if (releasedCount > 0) {
            throw new ServiceException(ApiError.ERROR_95029);
        }
        boolean flag = projectTaskService.updateTaskState(taskIds, state,null,null);
        return flag;
    }
}
