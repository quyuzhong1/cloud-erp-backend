package com.erp.server.scm.service;

import com.common.business.service.SuperService;
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
     * @description: 根据采购申请明细ids查询
     * @author Will
     * @date: 2023/3/28 20:12
     * @param dto
     * @return List<ListDTO>
     */
    List<PurchaseApplicationRefPoDTO.ListDTO> list(PurchaseApplicationRefPoDTO.SearchParamDTO dto);
    /**
     * @description: 根据采购订单Ids查询
     * @author Will
     * @date: 2023/3/29 10:46
     * @param purchaseOrderIds
     * @return List<PurchaseApplicationRefPoEntity>
     */
    List<PurchaseApplicationRefPoEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds);
    /**
     * @description: 根据采购订单ids删除
     * @author Will
     * @date: 2023/3/29 12:04
     * @param purchaseOrderIds
     */
    void removeByPurchaseOrderIds(List<String> purchaseOrderIds);
    /**
     * @description: 根据采购订单明细ids删除
     * @author Will
     * @date: 2023/3/29 18:51
     * @param purchaseOrderDetailIds
     */
    void removeByPurchaseOrderDetailIds(List<String> purchaseOrderDetailIds);
}
