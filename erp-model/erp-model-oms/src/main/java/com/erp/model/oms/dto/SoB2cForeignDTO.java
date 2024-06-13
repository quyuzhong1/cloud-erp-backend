package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * b2c订单外部调用dto
 */
public class SoB2cForeignDTO implements Serializable {


    /**
     * 查询发货信息REQ
     */
    @Data
    @NoArgsConstructor
    public static class OrderDeliveryReq {

        /**
         * 店铺Id(第三方平台)
         */
        private String shopId;

        private String erpShopId;

        /**
         * 平台订单号，多个用逗号隔开
         */
        private String platformOrderCode;

        private List<String> platformOrderCodeList;
        /**
         * 卖家订单编号，多个用逗号隔开
         */
        private String sellerOrderCode;

        private List<String> sellerOrderCodeList;
        /**
         * 订单状态
         *  waitDistribution : 待配货
         *  inDistribution : 配货中,
         *  waitShipped : 待发货,
         *  shipped : 已发货,
         *  partialShipped : 部分发货,
         *  frozen : 冻结中,
         */
        private String orderStatus;

        /**
         * 审核状态
         *  waitSubmit : 待提交,
         *  approveIng : 审核中,
         *  reject : 审核不通过,
         *  approve : 已审核;
         */
        private String approveStatus;

        /**
         * 发货时间开始时间 yyyy-MM-dd HH:mm:ss
         */
        private LocalDateTime deliveryStartTime;


        /**
         * 发货时间结束时间 yyyy-MM-dd HH:mm:ss
         */
        private LocalDateTime deliveryEndTime;
    }

    /**
     * 查询发货信息REQ
     */
    @Data
    @NoArgsConstructor
    public static class OrderDeliveryResp {

        private Boolean invalidStatus;
        private String id;
        /**
         * 店铺Id(第三方平台)
         */
        private String shopId;

        /**
         * ERP系统订单编号
         */
        private String erpOrderCode;

        /**
         * 平台订单号
         */
        private String platformOrderCode;

        /**
         * 卖家订单编号
         */
        private String sellerOrderCode;

        /**
         * 订单来源
         *  selfAdd : ERP新增
         *  thirdPlatform : 第三方平台新增
         */
        private String orderSource;

        /**
         * 订单状态
         *  waitDistribution : 待配货
         *  inDistribution : 配货中,
         *  waitShipped : 待发货,
         *  shipped : 已发货,
         *  partialShipped : 部分发货,
         *  frozen : 冻结中,
         */
        private String orderStatus;

        /**
         * 审核状态
         *  waitSubmit :  待提交,
         *  approveIng :  审核中,
         *  reject :  审核不通过,
         *  approve :  已审核;
         */
        private String approveStatus;

        /**
         * 是否拆分订单
         */
        private Boolean isSplitOrder;

        /**
         * 拆分订单信息
         */
        private SplitOrderInfo splitOrderInfo;

        /**
         * 是否合并订单
         */
        private Boolean isMergeOrder;

        /**
         * 拆分订单信息
         */
        private List<MergeOrderInfo> mergeOrderInfoList;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 物流渠道id
         */
        private String logisticChannelId;

        /**
         * 物流渠道代码
         */
        private String logisticChannelCode;

        /**
         * 物流渠道名称
         */
        private String logisticChannelName;

        /**
         * 运单号
         */
        private String transportCode;

        /**
         * 跟踪号
         */
        private String trackingNumber;

        /**
         * 审核时间 yyyy-MM-dd HH:mm:ss
         */
        private LocalDateTime approveTime;

        /**
         * 发货时间 yyyy-MM-dd HH:mm:ss
         */
        private LocalDateTime deliveryTime;

    }

    /**
     * 合并订单信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MergeOrderInfo {

        /**
         * 合并子订单erp编号
         */
        private String mergeChildOrderErpOrderCode;

        /**
         * 合并子订单卖家订单编号
         */
        private String mergeChildOrderSellerOrderCode;

        /**
         * 合并子订单平台订单
         */
        private String mergeChildOrderPlatformOrderCode;
    }

    /**
     * 拆分订单信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SplitOrderInfo {

        /**
         * 拆分父订单erp编号
         */
        private String splitParentOrderErpOrderCode;

        /**
         * 拆分父订单卖家订单编号
         */
        private String splitParentOrderSellerOrderCode;

        /**
         * 拆分父订单平台订单
         */
        private String splitParentOrderPlatformOrderCode;

    }

}