package com.erp.server.plm.service;

import com.erp.model.plm.dto.TaskConcernDTO;
import com.erp.model.plm.entity.TaskConcernEntity;
import com.common.business.service.SuperService;

import java.util.List;

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

    /**
     * 保存关注人
     * @author yl
     * @date 2023-06-20 16:43
     * @param taskId
     * @param productId
     * @param refUserIdList
     * @return void
     */
    void batchAdd(String taskId, String productId, List<String> refUserIdList);

    /**
     * 更改任务关注人
     * @author yl
     * @date 2023-06-20 16:54
     * @param taskId
     * @param productId
     * @param refUserIdList
     * @return void
     */
    void batchUpdate(String taskId, String productId, List<String> refUserIdList);

    
    /**
     *
     *根据任务id 获取到对应关注人信息
     * @author yl
     * @date 2023-06-20 17:02
     * @param taskId
     * @return java.util.List<java.lang.String>
     */
    List<String> listByTaskId(String taskId);
}
