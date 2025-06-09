package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.RealOutOfStockEntity;
import com.common.business.service.SuperService;

import java.time.LocalDate;

/**
 * <p>
 * 真实断货报告 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface RealOutOfStockService extends SuperService<RealOutOfStockEntity> {

    /**
     * 根据补货建议id获取历史断货日期
     */
    LocalDate getRealStartDate(String id, LocalDate basicCalcDate);


    void dealRealOutOfStock(LocalDate calculationDate);

}
