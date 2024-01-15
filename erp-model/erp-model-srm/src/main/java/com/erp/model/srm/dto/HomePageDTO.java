package com.erp.model.srm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 首页数据DTO
 * </p>
 *
*/
@Data
@NoArgsConstructor
public class HomePageDTO implements Serializable {

    /**
    * 用户信息
    */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfoDTO {
        /**
         * 公司名称
         */
        private String companyName;

        /**
         * 公司状态
         */
        private String companyStatus;

        /**
         * 姓名
         */
        private String userName;

        /**
         * 电话
         */
        private String phone;

        /**
         * 微信昵称
         */
        private String wxName;

    }


    /**
     * 用户信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToDoItems {
        /**
         * 待确认订单数
         */
        private Integer waitConfirmOrderCount;

        /**
         * 待打印送货单数
         */
        private Integer waitPrintDeliveryCount;

        /**
         * 待确认送货单数
         */
        private Integer waitConfirmDeliveryCount;

        /**
         * 待确认对账单数
         */
        private Integer waitConfirmReconciliationCount;
    }


    /**
     * 统计数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistical {
        /**
         * 统计信息
         */
        private TotalInfo totalInfo;

        /**
         * 订单趋势list
         */
        private List<OrderTrend> orderTrendList;

        /**
         * 退货趋势list
         */
        private List<RefundTrend> refundTrendList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TotalInfo {
        /**
         * 订单量
         */
        private Integer orderCount;

        /**
         * 订单SKU量
         */
        private Integer orderSkuCount;

        /**
         * 订单额
         */
        private BigDecimal orderMoney;

        /**
         * 退货量
         */
        private Integer refundSkuCount;

        /**
         * sku质检退货量
         */
        private Integer skuQcRefundCount;

        /**
         * sku质检退货率
         */
        private BigDecimal skuQcRefundRate;

        /**
         * 退货总额
         */
        private BigDecimal refundMoney;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderTrend {
        /**
         * 月份
         */
        private String month;

        /**
         * 订单SKU量
         */
        private Integer orderSkuCount;

        /**
         * 订单额
         */
        private BigDecimal orderAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundTrend {
        /**
         * 月份
         */
        private String month;

        /**
         * 退货总量
         */
        private Integer refundSkuCount;

        /**
         * 质检退货量
         */
        private Integer qcRefundSkuCount;
    }

}