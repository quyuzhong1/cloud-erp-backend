package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductPlanSaleEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:05
 */
public interface ProductPlanSaleService extends IService<ProductPlanSaleEntity> {
    /**
     * @description: 根据规划删除销售信息
     * @author Will
     * @date: 2023/2/21 17:53
     * @param productPlanId
     * @return Boolean
     */
    Boolean removeByProductPlanId(String productPlanId);
    /**
     * @description: 根据规划id查询
     * @author Will
     * @date: 2023/2/21 18:16
     * @param productPlanId
     * @return ProductPlanSaleEntity 
     */
    ProductPlanSaleEntity getByProductPlanId(String productPlanId);
}
