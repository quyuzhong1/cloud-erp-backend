package com.erp.server.oms.service;

import com.erp.model.oms.entity.ListingInfoEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 对应平台sku 表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
public interface ListingInfoService extends SuperService<ListingInfoEntity> {

    /**
     * 根据 sku 获取到listing 数据
     * @author yl
     * @date 2023-08-18 16:35
     * @param skuNo
     * @return com.erp.model.oms.entity.ListingInfoEntity
     */
    ListingInfoEntity getBySkuNo(String skuNo,String type);

    /**
     * 添加库存sku
     * @param warehouseSkuNo
     * @param warehouseProductName
     * @return
     */
    String addWarehouseSku(String warehouseSkuNo, String warehouseProductName);
}
