package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;

import java.util.List;

/**
 * <p>
 * 采购退货单明细 服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
public interface PurchaseReturnOrderDetailService extends SuperService<PurchaseReturnOrderDetailEntity> {
    /**
     * @description: 根据来源明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:54
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     */
    List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);
}
