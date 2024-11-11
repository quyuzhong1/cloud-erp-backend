package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.FbaInventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * fba历史库存 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
public interface FbaHistoryInventoryService extends SuperService<FbaHistoryInventoryEntity> {

    void saveTodayInventory(List<FbaInventoryEntity> inventoryEntities, LocalDate calculationDate);

    List<FbaHistoryInventoryEntity> listBySkuNo(String skuNo, String fbaWarehouseId);
}
