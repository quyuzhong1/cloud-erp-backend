package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseApplicationDetailService extends SuperService<PurchaseApplicationDetailEntity> {
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/3/21 18:05
     * @param details
     * @param purchaseApplicationId
     */
    void add(List<PurchaseApplicationDetailDTO.AddDTO> details, String purchaseApplicationId);
    /**
     * @description: 更新明细
     * @author Will
     * @date: 2023/3/21 18:11
     * @param details
     * @param id
     */
    void update(List<PurchaseApplicationDetailDTO.UpdateDTO> details, String id);
    /**
     * @description: 根据采购申请主表id查询明细
     * @author Will
     * @date: 2023/3/21 18:14
     * @param purchaseApplicationId
     * @return List<PurchaseApplicationDetailEntity>
     */
    List<PurchaseApplicationDetailEntity> listByPurchaseApplicationId(String purchaseApplicationId);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/21 18:27
     * @param purchaseApplicationIds
     */
    void removeByPurchaseApplicationIds(List<String> purchaseApplicationIds);
    /**
     * @description: 根据主表id和skuId查询
     * @author Will
     * @date: 2023/3/22 10:11
     * @param purchaseApplicationId
     * @param skuId
     * @return PurchaseApplicationDetailEntity
     */
    PurchaseApplicationDetailEntity getByPurchaseApplicationIdAndSkuId(String purchaseApplicationId, String skuId);
    /**
     * @description: 查询可生成采购订单的明细
     * @author Will
     * @date: 2023/3/23 10:21
     * @param purchaseApplicationId
     * @return List<PurchaseApplicationDetailEntity> 
     */
    List<PurchaseApplicationDetailEntity> listCreatePurchaseOrderDetail(String purchaseApplicationId);
}
