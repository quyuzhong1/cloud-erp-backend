package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-28
 */
public interface PurchasePriceHistoryService extends SuperService<PurchasePriceHistoryEntity> {


    /**
     * 根据采购价目详情表id 获取历史数据
     * @author yl
     * @date 2023-03-29 10:28
     * @param priceDetailId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.HistoryDTO>
     */
    List<PurchasePriceDetailDTO.HistoryDTO> getHistory(String priceDetailId);

    /**
     * @description: 查询采购价目历史表报价
     * @author Will
     * @date: 2023/4/11 9:51
     * @param dto
     * @return List<PurchaseTaxPriceViewDTO>
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getHistoryTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-11 14:50
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     */
    List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId,List<String> skuIdList);

    /**
     * 方法说明（批量）
     * @Author Luo_WG
     * @Date 2024/1/9 14:23
     * @param supplierIds
     * @param skuIdList
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     **/
    List<PurchasePriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds,List<String> skuIdList);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-11 17:47
     * @param purchasePriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.PurchasePriceHistoryEntity>
     */
    List<PurchasePriceHistoryEntity> getHistoryByDetailIds(List<String> purchasePriceDetailIds);

    /**
     * 根据变更详情id 获取
     * @author yl
     * @date 2023-10-23 16:13
     * @param changeDetailIdList
     * @return java.util.List<com.erp.model.scm.entity.PurchasePriceHistoryEntity>
     */
    List<PurchasePriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList);
}
