package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 库存详情 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface ReplenishmentInventoryDetailService extends SuperService<ReplenishmentInventoryDetailEntity> {

    /**
     * 根据补货建议明细id查询库存明细
     */
    List<ReplenishmentInventoryDetailEntity> listByReplenishmentDetailIds(List<String> ids);
}
