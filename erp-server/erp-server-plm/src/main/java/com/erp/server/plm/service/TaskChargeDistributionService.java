package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/29 14:43
 */
public interface TaskChargeDistributionService extends IService<TaskChargeDistributionEntity> {

    /**
     * @description: 根据来源和任务id查询
     * @author Will
     * @date: 2023/1/29 14:53
     * @param source
     * @param taskId
     * @return List<TaskChargeDistributionEntity>
     */
    List<TaskChargeDistributionEntity> listBySourceAndTaskId(Integer source, String taskId);

   /**
    * @description:删除并重新新增
    * @author Will
    * @date: 2023/1/29 15:15
    * @param taskId
    * @param approvalList
    * @param source
    */
    void removeAndSave(String taskId, List<TaskChargeDistributionEntity> approvalList, Integer source);

    /**
     * @description: 根据来源和任务id进行删除
     * @author Will
     * @date: 2023/1/29 16:29
     * @param source
     * @param taskId
     */
    void removeBySourceAndTaskId(Integer source, String taskId);
}
