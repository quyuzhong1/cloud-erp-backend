package com.erp.server.scm.service;

import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 产品采购变更价 明细表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
public interface PurchasePriceChangeDetailService extends SuperService<PurchasePriceChangeDetailEntity> {


    /**
     * 检查区间报价是否存在
     *
     * @param purchasePriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-28 12:07
     */
    void checkSkuInterval(List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList);

    /**
     * 根据变更表id 获取明细
     *
     * @param id
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-28 14:35
     */
    List<PurchasePriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String id);

    /**
     * 添加明细
     * @author yl
     * @date 2023-03-28 16:05
     * @param id
     * @param purchasePriceChangeDetailList
     * @return void
     */
    void addPriceChangeDetail(String id, List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList);
}
