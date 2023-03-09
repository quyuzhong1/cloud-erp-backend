package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
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
     * 根据来源和任务id 集合查询
     * @author yl
     * @date 2023-02-28 14:30
     * @param source
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskChargeDistributionEntity>
     */
    List<TaskChargeDistributionEntity> listBySourceAndTaskIdList(Integer source, List<String> taskIds );

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
    /**
     * @description: 
     * @author Will
     * @date: 2023/2/15 13:06
     * @param source 
     * @param name 
     * @return List<TaskChargeDistributionEntity> 
     */
    List<TaskChargeDistributionEntity> listBySourceAndRoleName(List<Integer> source, String name);

    
    /**
     * 产品另存为模板 同步任务审核人
     * @author yl
     * @date 2023-03-09 20:15
     * @param taskSourceList
     * @return void
     */
    void syncTemplateTaskChargeDistribution(List<CopySourceDTO> taskSourceList);
}
