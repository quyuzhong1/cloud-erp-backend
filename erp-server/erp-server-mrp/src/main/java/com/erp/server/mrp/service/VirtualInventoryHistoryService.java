package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.VirtualInventoryHistoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.VirtualInventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 虚拟库存表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
public interface VirtualInventoryHistoryService extends SuperService<VirtualInventoryHistoryEntity> {

    /**
     * 保存每日库存
     * @param virtualInventory 虚拟仓库存
     * @param calculationDate 计算日
     */
    void saveTodayInventory(List<VirtualInventoryEntity> virtualInventory, LocalDate calculationDate);
}
