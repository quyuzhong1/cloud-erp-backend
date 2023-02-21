package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductPlanRemarkEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:04
 */
public interface ProductPlanRemarkService extends IService<ProductPlanRemarkEntity> {
    /**
     * @description: 根据规划删除备注信息
     * @author Will
     * @date: 2023/2/21 17:57
     * @param productPlanId
     * @return Boolean
     */
    Boolean removeByProductPlanId(String productPlanId);
    /**
     * @description: 根据规划查询
     * @author Will
     * @date: 2023/2/21 18:28
     * @param productPlanId
     * @return List<ProductPlanRemarkEntity>
     */
    List<ProductPlanRemarkEntity> listByProductPlanId(String productPlanId);
}
