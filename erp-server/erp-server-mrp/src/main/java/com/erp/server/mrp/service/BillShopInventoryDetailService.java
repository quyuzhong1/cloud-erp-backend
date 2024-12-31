package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.BillShopInventoryDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 店铺库存明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-31
 */
public interface BillShopInventoryDetailService extends SuperService<BillShopInventoryDetailEntity> {

    /**
     * 根据主表id和店铺查询店铺库存
     * @param ids id
     * @param shopId 店铺id
     */
    Map<String, Integer> listByMainIdsAndShopId(List<String> ids, String shopId);

    /**
     * 根据主表id和店铺统计店铺库存
     * @param ids id
     * @param shopId 店铺id
     */
    int totalByMainIdsAndShopId(List<String> ids, String shopId);
}
