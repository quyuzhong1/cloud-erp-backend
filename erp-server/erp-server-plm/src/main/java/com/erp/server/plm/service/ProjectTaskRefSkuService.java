package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
}
