package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderDetailService extends SuperService<PurchaseOrderDetailEntity> {
    /**
     * @description: 新增采购订单明细
     * @author Will
     * @date: 2023/3/24 10:56
     * @param details
     * @param purchaseOrderId
     */
    void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId);
}
