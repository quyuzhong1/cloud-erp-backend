package com.erp.model.scm.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SKU成本毛利
 * @CreateTime: 2023-07-04  16:51
 * @Author: zhangchunlin
 */
@Data
public class SkuCostProfitDTO implements Serializable {

    /**
     * 查询SKU成本、毛利参数
     */
    @Data
    public static class SkuCostProfitParam {

        /**
         * SKU ID
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 销售金额
         */
        @NotNull(message = "销售金额不能为空")
        private BigDecimal saleAmount;

        /**
         * 税率（不需要除以100）
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;

        /**
         * 币制
         */
        @NotEmpty(message = "币制不能为空")
        private String currency;

        /**
         * 单据日期
         */
        @NotNull(message = "销售订单单据日期不能为空")
        private LocalDate billDate;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 价税合计
         */
        private BigDecimal taxAmount;
        /**
         * 销售金额(折后)*汇率
         */
        private BigDecimal amountLocalCurrency;
    }


    /**
     * 查询SKU成本、毛利响应
     */
    @Data
    public static class SkuCostProfitResult {

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 采购单价
         */
        private BigDecimal purchasePrice;

        /**
         * 销售总成本
         */
        private BigDecimal saleCost;

        /**
         * 销售毛利
         */
        private BigDecimal saleProfit;

        /**
         * 销售毛利率
         */
        private BigDecimal saleProfitRate;

        /**
         * 本位币的计算汇率值
         */
        private BigDecimal exchangeRate;

        /**
         * 价税合计（折前）
         */
        private BigDecimal taxAmountBefore;

        /**
         * 价税合计额（折后）本位币
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         *  销售金额（折后）本位币
         */
        private BigDecimal amountLocalCurrency;


        /**
         * 销售金额（折后）
         */
        private BigDecimal saleAmount;

        /**
         * 价税合计（折后）
         */
        private BigDecimal taxAmount;



    }


}