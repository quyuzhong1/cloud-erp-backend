package com.erp.server.plm.service;

import com.erp.model.plm.dto.TaskConcernDTO;
import com.erp.model.plm.entity.TaskConcernEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 任务关注的人 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-19
 */
public interface TaskConcernService extends SuperService<TaskConcernEntity> {

    
    /**
     * 获取到任务id是否被登录人关注
     * @author yl
     * @date 2023-06-19 18:57
     * @param taskId
     * @return java.lang.Boolean
     */
    TaskConcernDTO.InfoDTO getConcernByTaskId(String taskId);

    /**
     * 任务关注
     * @param dto
     * @return
     */
    Boolean concernTask(TaskConcernDTO.ConcernDTO dto);

    /**
     * 取消任务关注
     * @param dto
     * @return
     */
    Boolean cancelConcern(TaskConcernDTO.ConcernDTO dto);


    /**
     * 根据任务id 统计到关注人数
     * @param taskId
     * @return
     */
    Integer getConcernCountByTaskId(String taskId);
}
