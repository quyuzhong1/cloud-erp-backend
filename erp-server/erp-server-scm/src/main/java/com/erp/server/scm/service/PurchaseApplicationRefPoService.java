package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;

import java.util.List;

/**
 * <p>
 * 采购申请单和采购订单关联表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseApplicationRefPoService extends SuperService<PurchaseApplicationRefPoEntity> {
    /**
     * 根据采购申请明细ids查询
     */
    List<PurchaseApplicationRefPoDTO.ListDTO> listByPurchaseApplicationDetailIds(List<String> detailIds);
}
