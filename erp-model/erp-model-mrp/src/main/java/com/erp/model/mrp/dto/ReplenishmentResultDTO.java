package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReplenishmentResultDTO {
    /**
     * 补货主表数据
     */
    private BasicDTO replenishment;
    /**
     * 补货明细表数据
     */
    private DetailDTO replenishmentDetail;
    /**
     * FBA在途明细
     */
    private List<FbaInTransitDetailDTO> fbaInTransitDetails;
    /**
     * 备货期
     */
    private List<AvgSalesEstimateDTO> avgSalesEstimates;

    /**
     * 断货报告
     */
    private List<RptOutOfStockDTO> rptOutOfStocks;


    @Getter
    @Setter
    public static class BasicDTO {
        /**
         * id
         */
        private String id;
        /**
         * 平台类型
         */
        private String platformType;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 国家
         */
        private String country;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * fba店铺的仓库id
         */
        private String fbaWarehouseId;

        /**
         * 平台
         */
        private String platform;
    }

    @Getter
    @Setter
    public static class DetailDTO {
        /**
         * sku类型 新品/常规品
         */
        private String skuType;

        /**
         * fba可用
         */
        private Integer fbaUsableQty;

        /**
         * fba在途
         */
        private Integer fbaInTransitQty;

        /**
         * FBA 预计发货数量
         */
        private Integer fbaPlanDeliveryQty;

        /**
         * 海外仓可用
         */
        private Integer overseasUsableQty;

        /**
         * 海外仓在途
         */
        private Integer overseasInTransitQty;

        /**
         * 海外仓预计发货
         */
        private Integer overseasPlanDeliveryQty;

        /**
         * 本地仓可用
         */
        private Integer localUsableQty;

        /**
         * 本地仓在途
         */
        private Integer localInTransitQty;

        /**
         * 本地仓预计采购
         */
        private Integer localPlanPurchaseQty;

        /**
         * 总库存
         */
        private Integer totalInventoryQty;

        /**
         * 分时段销量  json
         */
        private String salesQty;

        /**
         * 分时段日均销  json
         */
        private String avgSalesQty;

        /**
         * 预估销量 json
         */
        private String salesEstimateQty;

        /**
         * 预估日销量  json
         */
        private String avgSalesEstimateQty;

        /**
         * 采购审批天数（天）
         */
        private Integer purchaseApproveDays;

        /**
         * 生产周期天数（天）
         */
        private Integer productionDays;

        /**
         * 供应商发货天数（天）
         */
        private Integer supplierDeliveryDays;

        /**
         * 质检入库天数（天）
         */
        private Integer qcDays;

        /**
         * 采购频率天数（天）
         */
        private Integer purchaseCycleDays;
        /**
         * 最短发货时效（天）
         */
        private Integer deliveryMinDays;
        /**
         * 默认发货时效（天）
         */
        private Integer deliveryDefaultDays;
        /**
         * 最长发货时效（天）
         */
        private Integer deliveryMaxDays;

        /**
         * 安全天数（天）
         */
        private Integer safeDays;

        /**
         * 入库天数（天）
         */
        private Integer instockDays;

        /**
         * fba可售天数
         */
        private Integer fbaSellableDays;

        /**
         * 可售天数
         */
        private Integer sellableDays;

        /**
         * 海外仓可售天数
         */
        private Integer overseasSellableDays;

        /**
         * 本地可售天数
         */
        private Integer localSellableDays;

        /**
         * 总库存可售天数
         */
        private Integer totalSellableDays;

        /**
         * 计算版本  所有子表加   根据单号生成规则
         */
        private String calcVersion;

        /**
         * 计算日期
         */
        private String calcDate;
    }

    @Getter
    @Setter
    public static class FbaInTransitDetailDTO {
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 发货状态
         */
        private String status;

        /**
         * 发货日期
         */
        private LocalDate deliveryDate;

        /**
         * 申报数量
         */
        private Integer declareQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 在途
         */
        private Integer inTransitQty;

        /**
         * 预计到货日期
         */
        private LocalDate planArrivalDate;

        /**
         * 计算版本  所有子表加   根据单号生成规则
         */
        private String calcVersion;

    }

    @Getter
    @Setter
    public static class AvgSalesEstimateDTO {

        private String type;

        private String qty;
    }

    @Getter
    @Setter
    public static class RptOutOfStockDTO {

        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 断货开始日期
         */
        private LocalDate startDate;

        /**
         * 断货结束日期
         */
        private LocalDate endDate;

        /**
         * 销量
         */
        private Integer salesQty;

        /**
         * 金额
         */
        private BigDecimal amount;
    }
}
