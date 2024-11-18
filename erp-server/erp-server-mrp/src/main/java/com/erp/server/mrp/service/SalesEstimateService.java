package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.SalesEstimateEntity;
import com.common.business.service.SuperService;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销量预估 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface SalesEstimateService extends SuperService<SalesEstimateEntity> {

    /**
     * 根据补货建议明细id查询距离今日多少天的数据
     * @param detailId 建议id
     * @param date 结束日期
     */
    List<SalesEstimateEntity> listByReplenishmentIdAndDay(String detailId, LocalDate date);
}
