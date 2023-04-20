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
    /**
     * @description: 根据采购订单id和skuid查询
     * @author Will
     * @date: 2023/3/27 15:02
     * @param purchaseOrderId 
     * @param skuId 
     * @return PurchaseOrderDetailEntity 
     */
    PurchaseOrderDetailEntity getByPurchaseOrderIdAndSkuId(String purchaseOrderId, String skuId);
    /**
     * @description: 修改采购订单明细
     * @author Will
     * @date: 2023/3/27 15:07
     * @param details
     * @param purchaseOrderId

     */
    void update(List<PurchaseOrderDetailDTO.UpdateDTO> details, String purchaseOrderId);
    /**
     * @description: 根据采购订单id查询
     * @author Will
     * @date: 2023/3/27 15:09
     * @param purchaseOrderId
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId);

    /**
     * @description: 根据采购订单ids查询
     * @author Will
     * @date: 2023/3/29 15:58
     * @param purchaseOrderIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds);

    /**
     * @description: 根据订单ids删除明细
     * @author Will
     * @date: 2023/3/27 15:29
     * @param purchaseOrderIds

     */
    void removeByPurchaseOrderIds(List<String> purchaseOrderIds);
    /**
     * @description: 根据ids更新
     * @author Will
     * @date: 2023/3/29 15:32
     * @param arrivalStatus
     * @param ids
     */
    void updateArrivalStatusByIds(String arrivalStatus, List<String> ids);

    /**
     * @description: 更新生成PO类型
     * @author Will
     * @date: 2023/4/3 17:05
     * @param purchaseOrderId
     */
    void updateCreatePoType (String purchaseOrderId);

    /**
     * 根据明细id查询明细
     * @Author Luo_WG
     * @Date 2023/4/13 14:00
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    List<PurchaseOrderDetailEntity> listDetailByIds(List<String> ids);

    /**
     * 根据主表Id查询明细
     * @Author Luo_WG
     * @Date 2023/4/20 18:37
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(String id);

    /**
     * @description: 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:25
     * @param dto
     * @return ViewProductDTO
     */
    List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(PurchaseOrderDetailDTO.ProductSearchParamDTO dto);
}
