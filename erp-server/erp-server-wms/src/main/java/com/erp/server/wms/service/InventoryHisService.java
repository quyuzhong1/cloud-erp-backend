package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;

import java.time.LocalDate;
import java.util.List;

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
     * @param flowList 需要重算流水
     * @param hisEntity 基准历史库存
     */
    void overrideInventoryHis(List<TransactionFlowEntity> flowList, InventoryHisEntity hisEntity);
}
