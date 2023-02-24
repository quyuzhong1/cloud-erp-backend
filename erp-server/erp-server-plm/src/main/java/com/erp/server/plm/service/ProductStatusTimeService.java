package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductStatusTimeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:02
 */
public interface ProductStatusTimeService extends IService<ProductStatusTimeEntity> {
    /**
     * @description: 新增或修改状态及时间
     * @author Will
     * @date: 2023/2/24 10:53
     * @param productId
     * @param approvalStatus
     * @return Boolean
     */
    void saveOrUpdateProductStatusTime(String productId, Integer approvalStatus);
}
