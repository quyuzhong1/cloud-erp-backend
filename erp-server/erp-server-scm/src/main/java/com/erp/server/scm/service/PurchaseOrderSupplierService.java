package com.erp.server.scm.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderSupplierService extends SuperService<PurchaseOrderSupplierEntity> {
    /**
     * @description: 根据采购订单ids查询
     * @author Will
     * @date: 2023/3/28 17:25
     * @param purchaseOrderIds

     */
    void deleteByPurchaseOrderIds(List<String> purchaseOrderIds);
    /**
     * @description: 根据订单主表id查询供应商
     * @author Will
     * @date: 2023/3/28 17:28
     * @param purchaseOrderId
     * @return PurchaseOrderSupplierEntity
     */
    PurchaseOrderSupplierEntity getByPurchaseOrderId(String purchaseOrderId);

    /**
     * @description: 根据订单主表ids查询供应商
     * @author Will
     * @date: 2023/3/29 16:22
     * @param ids
     * @return List<PurchaseOrderSupplierEntity>
     */
    List<PurchaseOrderSupplierEntity> listByPurchaseOrderIds(List<String> ids);

    /**
     * @description:新增供应商
     * @author Will
     * @date: 2023/3/28 19:41
     * @param purchaseOrderSupplierDTO
     */
    void add(PurchaseOrderSupplierDTO.AddDTO purchaseOrderSupplierDTO,String purchaseOrderId);
    /**
     * @description: 修改供应商
     * @author Will
     * @date: 2023/3/28 19:49
     * @param purchaseOrderSupplierDTO
     */
    void update(PurchaseOrderSupplierDTO.UpdateDTO purchaseOrderSupplierDTO,String purchaseOrderId);

    /**
     * 获取供应商采购记录
     * @author yl
     * @date 2023-03-29 10:50
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchaseOrderSupplierDTO.SupplierPurchaseDTO>
     */
    PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> supplierPurchasePaging(PagingDTO<BaseIdDTO> dto);

}
