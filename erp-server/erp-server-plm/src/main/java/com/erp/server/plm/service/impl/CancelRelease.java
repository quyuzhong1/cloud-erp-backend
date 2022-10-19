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
 * 取消发布
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 15:51
 * @Created by yl
 */
@Service
public class CancelRelease implements TaskOperateStrategy {


    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * 取消发布任务
     * 在未开始的时候 可以取消发布 否则 不行，取消发布后变为待发布  任务状态为待发布
     *
     * @param taskIds
     * @param state
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-18 15:51
     */
    @Override
    public Boolean updateTaskState(List<String> taskIds, Integer state,String userId) {
        //待开始
        Integer notStartCode = TaskStateEnum.NOT_START.getCode();
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //统计项目状态为  不是待发布的任务
        long releasedCount = list.stream().filter(t -> !notStartCode.equals(t.getStatus())).count();
        if (releasedCount > 0) {
            throw new ServiceException(ApiError.ERROR_95029);
        }
        boolean flag = projectTaskService.updateTaskState(taskIds, state,null,null);
        return flag;
    }
}
