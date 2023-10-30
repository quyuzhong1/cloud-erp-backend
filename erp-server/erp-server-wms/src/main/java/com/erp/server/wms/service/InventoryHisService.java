package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryHisEntity;

import java.time.LocalDate;

/**
 * @Classname: InventoryHisService

 * @CreateTime: 2023-04-27  17:03
 * @Author: zhangchunlin
 */
public interface InventoryHisService extends SuperService<InventoryHisEntity> {

    /**
     * 根据库存表id和单据日期获取数据
     * @param infoId
     * @param billDate
     * @return
     */
    InventoryHisEntity findInventory(String infoId, LocalDate billDate);

    /**
     * 修改库存历史表数量
     * @param id
     * @param qty（变更数量）
     * @return
     */
    int updateQtyById(String id, Integer qty);

    /**
     * 新增或修改库存历史
     * @param inventoryInfoId
     * @param billDate
     * @param inventoryQty
     */
    void addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer inventoryQty);

    /**
     * 获取最近一次的历史库存
     * @param inventoryId
     * @param localDate
     * @return
     */
    InventoryHisEntity findLastInventory(String inventoryId, LocalDate localDate);

    /**
     * 历史库存重算方法
     * @param startTime
     * @param endTime
     * @param status
     * @param inventoryId
     */
    void overrideInventoryHis(LocalDate startTime, LocalDate endTime, String status, String inventoryId);
}
