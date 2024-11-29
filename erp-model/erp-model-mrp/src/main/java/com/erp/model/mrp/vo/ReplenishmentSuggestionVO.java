package com.erp.model.mrp.vo;

import com.common.business.annotation.Dict;
import com.erp.model.mrp.enums.ReplenishmentTypeEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReplenishmentSuggestionVO {
    /**
     * 补货建议主表id
     */
    private String id;

    @Getter
    @Setter
    public static class PagingView {
        /**
         * 补货建议主表id
         */
        private String id;
        /**
         * 补货建议详细id
         */
        private String detailId;
        /**
         * 关注
         */
        private Boolean favorite;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;
        /**
         * sku图片
         */
        private String skuImgUrl;
        /**
         * 品名
         */
        private String productName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名字
         */
        private String countryName;
        /**
         * 国家图片
         */
        private String countryImgUrl;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;

        /**
         * 品牌名字
         */
        private String brandName;
        /**
         * 分类名字
         */
        private String categoryName;
        /**
         * 标签
         */
        private List<LabelVO> labels;
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
         * 销量分析
         */
        private SalesAnalysisVO salesAnalysis;
        /**
         * 分时段销量
         */
        private String salesQtyJson;
        private List<SalesVO> salesQty;

        /**
         * 分时段日均销
         */
        private String avgSalesQtyJson;
        private List<SalesVO> avgSalesQty;

        /**
         * 预估销量
         */
        private String salesEstimateQtyJson;
        private List<SalesVO> salesEstimateQty;
        /**
         * 预估日销量
         */
        private String avgSalesEstimateQtyJson;

        private List<SalesVO> avgSalesEstimateQty;
        /**
         * 运营月销量预估
         */
        private SalesEstimateManualVO salesEstimateManualVO;
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
         * 发货频率
         */
        private Integer logisticsCycleDays;
        /**
         * 最短发货频率
         */
        private Integer logisticsMinCycleDays;
        /**
         * 最长发货频率
         */
        private Integer logisticsMaxCycleDays;
        /**
         * 物流方式
         */
        private String logisticsMinMethod;
        /**
         * 物流方式名
         */
        private String logisticsMinMethodName;
        /**
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流方式名
         */
        private String logisticsMethodName;
        /**
         * 物流方式
         */
        private String logisticsMaxMethod;
        /**
         * 物流方式名
         */
        private String logisticsMaxMethodName;

        /**
         * 最短备货时效（天）
         */
        private Integer stockUpMinDays;
        /**
         * 默认备货时效（天）
         */
        private Integer stockUpDefaultDays;
        /**
         * 最长备货时效（天）
         */
        private Integer stockUpMaxDays;

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
         * 断货日
         */
        private DateVO outOfStockDay;
        /**
         * 建议发货日
         */
        private DateVO suggestShippingDate;
        /**
         * 建议发货量
         */
        private Integer suggestShippingQty;
        /**
         * 建议采购日
         */
        private DateVO suggestPurchaseDate;
        /**
         * 建议采购量
         */
        private Integer suggestPurchaseQty;
        /**
         * 是否补货
         */
        @Dict(enumClass = ReplenishmentTypeEnum.class)
        private String replenishmentType;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 备注
         */
        private String remark;
        /**
         * fba在途配置
         */
        private String cfgFbaInTransit;
        /**
         * 明细配置
         */
        private String cfgRule;
    }

    @Getter
    @Setter
    public static class View {
        /**
         * 补货建议主表id
         */
        private String id;
        /**
         * 补货建议详细id
         */
        private String detailId;
        /**
         * 关注
         */
        private Boolean favorite;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;
        /**
         * sku图片
         */
        private String skuImgUrl;
        /**
         * 品名
         */
        private String productName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名字
         */
        private String countryName;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 标签
         */
        private List<LabelVO> labels;
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
         * 分时段销量
         */
        private String salesQtyJson;
        private List<SalesVO> salesQty;

        /**
         * 分时段日均销
         */
        private String avgSalesQtyJson;
        private List<SalesVO> avgSalesQty;

        /**
         * 预估销量
         */
        private String salesEstimateQtyJson;
        private List<SalesVO> salesEstimateQty;
        /**
         * 预估日销量
         */
        private String avgSalesEstimateQtyJson;
        private List<SalesVO> avgSalesEstimateQty;
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
         * 发货频率
         */
        private Integer logisticsCycleDays;
        /**
         * 最短发货频率
         */
        private Integer logisticsMinCycleDays;
        /**
         * 最长发货频率
         */
        private Integer logisticsMaxCycleDays;
        /**
         * 物流方式
         */
        private String logisticsMinMethod;
        /**
         * 物流方式名
         */
        private String logisticsMinMethodName;
        /**
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流方式名
         */
        private String logisticsMethodName;
        /**
         * 物流方式
         */
        private String logisticsMaxMethod;
        /**
         * 物流方式名
         */
        private String logisticsMaxMethodName;
        /**
         * 最短备货时效（天）
         */
        private Integer stockUpMinDays;
        /**
         * 默认备货时效（天）
         */
        private Integer stockUpDefaultDays;
        /**
         * 最长备货时效（天）
         */
        private Integer stockUpMaxDays;

        /**
         * 安全天数（天）
         */
        private Integer safeDays;

        /**
         * 入库天数（天）
         */
        private Integer instockDays;
        /**
         * 是否补货
         */
        @Dict(enumClass = ReplenishmentTypeEnum.class)
        private String replenishmentType;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 备注
         */
        private String remark;
        /**
         * fba在途配置
         */
        private String cfgFbaInTransit;

        /**
         * 配置
         */
        private String cfgRule;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesVO {
        /**
         * 类型
         */
        private String type;
        /**
         * 数量
         */
        private BigDecimal qty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateVO {
        /**
         * 预警类型
         */
        private String type;
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 天数
         */
        private Integer days;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesAnalysisVO {
        /**
         * 日期
         */
        private List<LocalDate> date;
        /**
         * 数量
         */
        private List<BigDecimal> qty;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SalesQtyTypeDTO {
        /**
         * sales_qty_type
         * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
         */
        private String salesQtyType;
        /**
         * 订单类型，all:全部，fba:FBA,fbm:FBM
         */
        private String orderType;
    }
}
