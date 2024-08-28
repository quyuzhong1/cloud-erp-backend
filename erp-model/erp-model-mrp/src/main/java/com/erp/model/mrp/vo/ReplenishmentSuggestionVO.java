package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

public class ReplenishmentSuggestionVO {

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
         * 平台
         */
        private String platform;

        /**
         * 品牌
         */
        private String brandId;

        /**
         * 分类
         */
        private String categoryId;

        /**
         * 是否补货
         */
        private String restock;

        /**
         * 备注
         */
        private String remark;
        /**
         * 主表id
         */
        private String mainId;

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

    }
}
