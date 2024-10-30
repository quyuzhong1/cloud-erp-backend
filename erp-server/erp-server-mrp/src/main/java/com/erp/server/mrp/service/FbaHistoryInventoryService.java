package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
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

    /**
     * 保存每日FBA库存
     *
     * @param inventoryEntities
     * @param calculationDate
     */
    void saveTodayInventory(List<FbaInventoryEntity> inventoryEntities, LocalDate calculationDate);


    /**
     * 查询库存，根据计算日
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<FbaHistoryInventoryEntity> listByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);
}
