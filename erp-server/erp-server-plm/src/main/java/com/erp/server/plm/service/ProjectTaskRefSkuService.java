package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TaskFinishSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProjectTaskRefSkuEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:54
 */
public interface ProjectTaskRefSkuService extends IService<ProjectTaskRefSkuEntity> {

    void addTaskSkuRef(String taskId, String productId, List<String> refSkuIdList);

    List<ProjectTaskRefSkuEntity> getByTaskId(String taskId);

    void saveBatchTaskRefSku(List<String> addTaskIdList, String productId,List<ProductDetailEntity> skuList);

    List<ProjectTaskRefSkuEntity> getByProductId(String productId);

    List<String> checkTaskRefSkuFinish(List<String> taskIdList);

    List<ProjectTaskRefSkuEntity> getByTaskIdList(List<String> noProcessTaskIds);

    void taskFinishRefSku(TaskFinishSkuDTO dto);
    /**
     * @description: 根据skuid查询
     * @author Will
     * @date: 2022/12/8 18:42
     * @param skuId
     * @return List<ProjectTaskRefSkuEntity>
     */
    List<ProjectTaskRefSkuEntity> listBySkuId(String skuId);

    void batchUpdate(String productId, List<String> taskIdList, List<String> refSkuIdList);
    
    /**
     * 根据任务id 删除对应关系
     * @author yl
     * @date 2023-03-13 17:30
     * @param taskId
     * @return void
     */
    void removeTaskSkuRefByTaskId(String taskId);
}
