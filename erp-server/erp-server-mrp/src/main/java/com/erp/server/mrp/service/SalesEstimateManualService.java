package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.SalesEstimateManualEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 运营销量预估 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface SalesEstimateManualService extends SuperService<SalesEstimateManualEntity> {

    /**
     * 查询运营销量预估
     * @param detailIds 补货建议明细id
     */
    List<SalesEstimateManualEntity> listByReplenishmentDetailIds(List<String> detailIds);
}
