package com.erp.server.oms.utils;

import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 销售订单工具类
 * @CreateTime: 2023-07-04  17:18
 * @Author: zhangchunlin
 */
public class SoUtils {

    /**
     * 计算成本毛利
     * @param purchasePrice
     * @param costParam
     * @param skuCostProfitResult
     */
    public static SkuCostProfitDTO.SkuCostProfitResult  calCostProfit(BigDecimal purchasePrice,
                                     SkuCostProfitDTO.SkuCostProfitParam costParam,
                                     SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult) {
        skuCostProfitResult.setPurchasePrice(purchasePrice);
        // 当没有计算得到采购单价或为0，所有都返回0
        if(Objects.isNull(skuCostProfitResult.getPurchasePrice()) || skuCostProfitResult.getPurchasePrice().compareTo(BigDecimal.ZERO) <=0 ) {
            return skuCostProfitResult;
        }
        skuCostProfitResult.setSaleCost(skuCostProfitResult.getPurchasePrice().multiply(new BigDecimal(costParam.getQty())).setScale(4, BigDecimal.ROUND_HALF_UP));
        if(Objects.isNull(costParam.getTaxRate())) {
            costParam.setTaxRate(BigDecimal.ZERO);
        }

        // 不含税销售额
        // BigDecimal noTaxAmount = costParam.getSaleAmount().divide(BigDecimal.ONE.add(costParam.getTaxRate().divide(new BigDecimal("100"))), 4, BigDecimal.ROUND_HALF_UP);
        // 销售毛利
        // skuCostProfitResult.setSaleProfit(noTaxAmount.subtract(skuCostProfitResult.getSaleCost()).setScale(4, BigDecimal.ROUND_HALF_UP));
        // 销售毛利
        BigDecimal saleAmount = costParam.getSaleAmount();
        BigDecimal saleCost = skuCostProfitResult.getSaleCost();
        BigDecimal saleProfit = saleAmount.subtract(saleCost).setScale(4, BigDecimal.ROUND_HALF_UP);
        skuCostProfitResult.setSaleProfit(saleProfit);
        // 销售毛利率
        if(costParam.getSaleAmount().compareTo(BigDecimal.ZERO) > 0) {
            skuCostProfitResult.setSaleProfitRate(skuCostProfitResult.getSaleProfit().divide(costParam.getSaleAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
        }
        return skuCostProfitResult;
    }

    /**
     * 销售订单明细成本
     * @param item
     */
    public static void updateSoDetailCost(SoDetailEntity item, BigDecimal purchasePrice, BigDecimal saleAmount) {
        SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult = new SkuCostProfitDTO.SkuCostProfitResult();
        skuCostProfitResult.setSkuId(item.getSkuId());
        skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
        skuCostProfitResult.setSaleCost(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfit(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfitRate(BigDecimal.ZERO);

        SkuCostProfitDTO.SkuCostProfitParam costParam = new SkuCostProfitDTO.SkuCostProfitParam();
        costParam.setSkuId(item.getSkuId());
        // 转换币制后的金额
        costParam.setSaleAmount(saleAmount);
        costParam.setQty(item.getQty());
        costParam.setTaxRate(item.getTaxRate());


        SoUtils.calCostProfit(purchasePrice,costParam,skuCostProfitResult);

        item.setPurchasePrice(skuCostProfitResult.getPurchasePrice());
        item.setSaleCost(skuCostProfitResult.getSaleCost());
        item.setSaleProfit(skuCostProfitResult.getSaleProfit());
        item.setSaleProfitRate(skuCostProfitResult.getSaleProfitRate());
    }

}