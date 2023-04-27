package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryDetailEntity;

import java.time.LocalDate;

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
    InventoryDetailEntity findByInfoIdAndInstockBatchDate(String inventoryInfoId, LocalDate instockBatchDate);

}
