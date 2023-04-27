package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryHisEntity;

import java.time.LocalDate;

/**
 * @Classname: InventoryHisService
 * @Description: TODO
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
    InventoryHisEntity findByInfoIdAndBillDate(String infoId, LocalDate billDate);

    /**
     * 修改库存历史表数量
     * @param id
     * @param qty（操作的数量，如果是扣减需传负数）
     * @param version
     * @return
     */
    int updateQtyById(String id, Integer qty, Integer version);

}
