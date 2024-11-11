package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.OverseasInventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 海外仓库存 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
public interface OverseasHistoryInventoryService extends SuperService<OverseasHistoryInventoryEntity> {

    /**
     * 保存每日库存
     * @param overseasHistoryInventory 海外库存
     * @param calculationDate 计算日期
     */
    void saveTodayInventory(List<OverseasInventoryEntity> overseasHistoryInventory, LocalDate calculationDate);
}
