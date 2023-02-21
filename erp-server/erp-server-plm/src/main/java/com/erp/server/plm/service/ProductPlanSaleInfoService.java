package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductPlanSaleInfoEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:06
 */
public interface ProductPlanSaleInfoService extends IService<ProductPlanSaleInfoEntity> {
    /**
     * @description: 根据规划删除销售数据
     * @author Will
     * @date: 2023/2/21 17:55
     * @param productPlanId
     * @return Boolean
     */
    Boolean removeByProductPlanId(String productPlanId);
    /**
     * @description: 根据规划id查询
     * @author Will
     * @date: 2023/2/21 18:20
     * @param productPlanId
     * @return List<ProductPlanSaleInfoEntity>
     */
    List<ProductPlanSaleInfoEntity> listByProductPlanId(String productPlanId);
}
