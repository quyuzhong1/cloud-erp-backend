package com.erp.server.oms.utils;

import cn.hutool.core.util.StrUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 销售订单工具类
 * @CreateTime: 2023-07-04  17:18
 * @Author: zhangchunlin
 */
@Slf4j
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

    /**
     * 计算折扣额信息等
     * @param discountAmount
     * @param saveOrUpdateList
     */
    public static void handleDetailAmount(BigDecimal discountAmount, List<SoDetailEntity> saveOrUpdateList) {
        // 折扣总额
        discountAmount = Objects.nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
        // 总的价税合计（折前）
        BigDecimal totalTaxAmountBefore = BigDecimal.ZERO;

        for(int i = 0;i < saveOrUpdateList.size();i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if(Objects.isNull(taxRate)) {
                taxRate = BigDecimal.ZERO;
            }
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            //价税合计（折前）
            BigDecimal taxAmount = MathUtil.multiply(taxPrice, qty);
            totalTaxAmountBefore = totalTaxAmountBefore.add(taxAmount).setScale(4, BigDecimal.ROUND_HALF_UP);
        }

        // 此处需要注意，所有的明细折扣额汇总起来需等于总的折扣额
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (int i = 0; i < saveOrUpdateList.size(); i ++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            item.setPrice(price);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);
            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);

            //含税金额（折扣前）
            BigDecimal taxAmount = MathUtil.multiply(taxPrice, qty);
            item.setTaxAmountBefore(taxAmount);
            item.setAmount(amount);

            // 折扣比例
            BigDecimal taxAmountRate;
            BigDecimal detailDiscountAmount = BigDecimal.ZERO;
            if(Objects.nonNull(discountAmount) && totalTaxAmountBefore.compareTo(BigDecimal.ZERO) == 1) {
                taxAmountRate = taxAmount.divide(totalTaxAmountBefore,10, BigDecimal.ROUND_HALF_UP);
                detailDiscountAmount = discountAmount.multiply(taxAmountRate).setScale(2, BigDecimal.ROUND_DOWN);
                log.warn("销售订单明细第【{}】条数据，价税合计（折扣前）比例【{}】，折扣额【{}】", (i + 1), taxAmountRate, detailDiscountAmount);
            }
            totalDiscountAmount = totalDiscountAmount.add(detailDiscountAmount).setScale(2, BigDecimal.ROUND_DOWN);
            // 最后一行，判断是否明细折扣额汇总是否等于总的折扣额
            if(i == saveOrUpdateList.size() - 1) {
                log.warn("销售订单明细汇总折扣额【{}】，总折扣额【{}】",totalDiscountAmount, discountAmount);
                if(discountAmount.compareTo(totalDiscountAmount) == 1) {
                    BigDecimal diff = discountAmount.subtract(totalDiscountAmount).setScale(2, BigDecimal.ROUND_DOWN);
                    detailDiscountAmount =  detailDiscountAmount.add(diff).setScale(2, BigDecimal.ROUND_DOWN);
                }
            }
            item.setDiscountAmount(detailDiscountAmount);
            // 价税合计（折扣后）
            taxAmount = taxAmount.subtract(detailDiscountAmount).setScale(4, BigDecimal.ROUND_HALF_UP);
            item.setTaxAmount(taxAmount);
            // 销售金额（折扣后）
            amount = amount.subtract(detailDiscountAmount).setScale(4, BigDecimal.ROUND_HALF_UP);
            item.setAmount(amount);
            // 折扣金额不可大于价税合计（折扣前）
            if(Objects.nonNull(detailDiscountAmount) && detailDiscountAmount.compareTo(item.getTaxAmountBefore()) == 1) {
                log.warn("销售订单第【{}】行明细价税合计（折扣前）【{}】，折扣额【{}】",( i + 1),item.getTaxAmountBefore(), detailDiscountAmount);
                throw new ServiceException(StrUtil.format("销售订单明细第{}行折扣金额【{}】不可大于价税合计（折前））【{}】", ( i + 1), detailDiscountAmount, item.getTaxAmountBefore()));
            }
        }
        // 折扣金额不可大于价税合计（折扣前）
        if(Objects.nonNull(discountAmount) && discountAmount.compareTo(totalTaxAmountBefore) == 1) {
            log.warn("销售订单明细汇总价税合计（折扣前）【{}】，总折扣额【{}】", totalTaxAmountBefore, discountAmount);
            throw new ServiceException(StrUtil.format("折扣金额【{}】不可大于价税合计（折前）【{}】", discountAmount, totalTaxAmountBefore));
        }
    }

}