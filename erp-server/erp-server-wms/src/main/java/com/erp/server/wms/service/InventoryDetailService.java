package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryDetailEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * @Classname: InventoryDetailService
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:00
 * @Author: zhangchunlin
 */
public interface InventoryDetailService  extends SuperService<InventoryDetailEntity> {

    /**
     * 根据库存表id和批次日期查询是否存在
     * @param inventoryInfoId
     * @param instockBatchDate
     * @return
     */
    InventoryDetailEntity findOneDetail(String inventoryInfoId, LocalDate instockBatchDate);

    /**
     * 根据库存表id+qty>0获取库存明细数据
     * @param inventoryInfoId
     * @return
     */
    List<InventoryDetailEntity> findListQtyGreatZero(String inventoryInfoId);

    /**
     * 修改库存明细表数量
     * @param id
     * @param qty（操作的数量，如果是扣减需传负数）
     * @param version
     * @return
     */
    int updateQtyById(String id, Integer qty, Integer version);

    /**
     * 新增或修改库存明细
     * @param inventoryInfoId
     * @param billDate
     * @param qty
     * @return
     */
    String addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer qty);

}
