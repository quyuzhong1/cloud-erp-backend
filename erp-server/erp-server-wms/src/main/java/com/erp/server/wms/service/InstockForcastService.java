package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.entity.InstockForcastEntity;

import java.util.List;

/**
 * <p>
 * 入库预报表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
public interface InstockForcastService extends SuperService<InstockForcastEntity> {


    /**
     * 根据采购订单生成入库预报单
     * @param dto
     */
    void generateByPurchaseOrder(InstockForcastDTO.AddDTO dto);

    /**
     * 根据采购订单id查询入库预报
     * @param purchaseOrderId
     * @return
     */
    InstockForcastEntity findByPurchaseOrderId(String purchaseOrderId);

    /**
     * 采购订单反审核（把入库预报置为已删除）
     * @param purchaseOrderId
     */
    void purchaseOrderUnApprove(String purchaseOrderId);

    /**
     * 采购订单批量反审核
     * @param purchaseOrderIds
     * @return
     */
    void purchaseOrderUnApproveBatch(List<String> purchaseOrderIds);

    /**
     * 采购订单结束交货
     * @param dto
     */
    void finishDelivery(InstockForcastDTO.FinishDeliveryDTO dto);




}
