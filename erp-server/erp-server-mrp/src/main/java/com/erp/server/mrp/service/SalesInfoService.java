package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.SalesInfoEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 历史销量信息 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface SalesInfoService extends SuperService<SalesInfoEntity> {

    /**
     * 获取历史销量
     * @param ids 补货建议明细id
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    List<SalesInfoEntity> listByReplenishmentDetailIds(List<String> ids, LocalDate startDate, LocalDate endDate);
    /**
     * 查询历史销量
     * @author will
     * @date 2024/9/8 14:20
     * @param detailIdList
     * @return List<SalesInfoEntity>
     */
    List<SalesInfoEntity> listHistorySalesInfo(List<String> detailIdList);

    /**
     * 获取历史销量
     * @param ids 补货建议明细id
     */
    List<SalesInfoEntity> listByReplenishmentDetailIds(List<String> ids);
}
