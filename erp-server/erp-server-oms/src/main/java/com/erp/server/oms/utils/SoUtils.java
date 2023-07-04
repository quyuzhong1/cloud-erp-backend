package com.erp.server.oms.utils;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 销售订单工具类
 * @CreateTime: 2023-07-04  17:18
 * @Author: zhangchunlin
 */
public class SoUtils {

    /**
     * 计算成本毛利
     * @param purchaseOrderDetailEntityList
     * @param costParam
     * @param skuCostProfitResult
     */
    public static void calCostProfit(List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList,
                                     SkuCostProfitDTO.SkuCostProfitParam costParam,
                                     SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult) {
        if(CollUtil.isNotEmpty(purchaseOrderDetailEntityList)) {
            skuCostProfitResult.setPurchasePrice(purchaseOrderDetailEntityList.get(0).getTaxPrice());
        }
        skuCostProfitResult.setSaleCost(skuCostProfitResult.getPurchasePrice().multiply(new BigDecimal(costParam.getQty())).setScale(4, BigDecimal.ROUND_HALF_UP));
        if(Objects.isNull(costParam.getTaxRate())) {
            costParam.setTaxRate(BigDecimal.ZERO);
        }

        // 不含税销售额
        BigDecimal noTaxAmount = costParam.getSaleAmount().divide(BigDecimal.ONE.add(costParam.getTaxRate().divide(new BigDecimal("100"))), 4, BigDecimal.ROUND_HALF_UP);
        // 销售毛利
        skuCostProfitResult.setSaleProfit(noTaxAmount.subtract(skuCostProfitResult.getSaleCost()).setScale(4, BigDecimal.ROUND_HALF_UP));
        // 销售毛利率
        if(costParam.getSaleAmount().compareTo(BigDecimal.ZERO) > 0) {
            skuCostProfitResult.setSaleProfitRate(skuCostProfitResult.getSaleProfit().divide(noTaxAmount, 4, BigDecimal.ROUND_HALF_UP));
        }
    }

}