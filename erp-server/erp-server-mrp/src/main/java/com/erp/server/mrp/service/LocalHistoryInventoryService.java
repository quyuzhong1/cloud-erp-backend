package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 库存表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
public interface LocalHistoryInventoryService extends SuperService<LocalHistoryInventoryEntity> {

    /**
     * 保存每日本地库存
     * @param localHistoryInventory 本地历史库存
     * @param calculationDate 计算日期
     */
    void saveTodayInventory(List<InventoryEntity> localHistoryInventory, LocalDate calculationDate);
}
