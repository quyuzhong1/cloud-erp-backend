package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;

import java.util.List;

/**
 * <p>
 * 产品采购价格明细表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceDetailService extends SuperService<PurchasePriceDetailEntity> {

     
    /**
     *  检查sku 区间报价
     * @author yl
     * @date 2023-03-24 14:01
     * @param purchasePriceDetailList
     * @return void
     */
    void checkSkuInterval(List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList);

    
    /**
     * 添加明细
     * @author yl
     * @date 2023-03-24 15:02
     * @param id
     * @param purchasePriceDetailList
     * @return void
     */
    void addPriceDetail(String id, List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList);
    /**
     * @description: 根据供应商id和skuId查询是否存在符合条件的单价和税率
     * @author Will
     * @date: 2023/3/27 9:37
     * @param dto
     * @return PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO
     */
    PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);
}
