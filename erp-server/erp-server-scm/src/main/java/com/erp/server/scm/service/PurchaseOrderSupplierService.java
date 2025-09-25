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
     * @param ids
     * @return List<PurchaseOrderSupplierEntity>
     * @description: 根据订单主表ids查询供应商
     * @author Will
     * @date: 2023/3/29 16:22
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

    /**
     * 根据供应商获取到 供应商订单信息
     * @author yl
     * @date 2023-04-03 17:11
     * @param supplierIdList
     * @return void
     */
    List<PurchaseOrderSupplierEntity>  getBySupplierIds(List<String> supplierIdList);

    /**
     *  检查采购订单是否有关联到供应商id
     *  如果有就不能删除
     * @author yl
     * @date 2023-04-14 11:55
     * @param supplierIds
     * @return void
     */
    void checkIsRefSupplier(List<String> supplierIds);
    /**
     * @description: 根据采购订单id集合查询供应商
     * @author Will
     * @date: 2024/1/25 10:16
     * @param idList
     * @return List<PurchaseOrderSupplierEntity>
     */
    List<PurchaseOrderSupplierEntity> listOrderSupplierByOrderIdList(List<String> idList);
}
