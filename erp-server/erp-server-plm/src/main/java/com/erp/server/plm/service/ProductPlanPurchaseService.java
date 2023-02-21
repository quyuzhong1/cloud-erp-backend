package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductPlanPurchaseEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:05
 */
public interface ProductPlanPurchaseService extends IService<ProductPlanPurchaseEntity> {
    /**
     * @description: 根据规划删除采购信息
     * @author Will
     * @date: 2023/2/21 17:45
     * @param productPlanId
     */
    Boolean removeByProductPlanId(String productPlanId);
    /**
     * @description: 根据规划id查询
     * @author Will
     * @date: 2023/2/21 18:12
     * @param id
     * @return ProductPlanPurchaseEntity
     */
    ProductPlanPurchaseEntity getByProductPlanId(String id);
}
