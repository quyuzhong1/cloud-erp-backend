package com.erp.model.mrp.dto;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.mrp.enums.TimePeriodEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
     * FBA到货明细
     */
    private List<EstimatedDeliveryDetailDTO> fbaDeliveryDetails;
    /**
     * 海外仓在途明细
     */
    private List<OverseasInTransitDetailDTO> overseasInTransitDetails;
    /**
     * 海外仓到货明细
     */
    private List<EstimatedDeliveryDetailDTO> overseasDeliveryDetails;
    /**
     * 本地在途明细
     */
    private List<LocalInTransitDetailDTO> localInTransitDetails;
    /**
     * 本地采购明细
     */
    private List<EstimatedPurchaseDetailDTO> localDeliveryDetails;
    /**
     * 备货期
     */
    private List<AvgSalesEstimateDTO> avgSalesEstimates;

    /**
     * 断货报告
     */
    private List<RptOutOfStockDTO> rptOutOfStocks;

    /**
     * 销量
     */
    private List<SalesInfoDTO> salesInfos;

    /**
     * 分时段销量
     */
    private List<TimePeriodSales> timePeriodSales;
    /**
     * 分时段日均销量
     */
    private List<TimePeriodSales> avgTimePeriodSales;

    /**
     * 销量预估
     */
    private List<SalesEstimateDTO> salesEstimates;
    /**
     * 建议发货
     */
    private List<DeliverySuggestDTO> deliverySuggests;

    /**
     * 建议采购
     */
    private List<PurchaseSuggestDTO> purchaseSuggests;

    /**
     * 采购单价
     */
    private BigDecimal purchasePrice;

    /**
     * 销售价
     */
    private BigDecimal salesPrice;

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
         * 明细id
         */
        private String detailId;
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
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

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
    @NoArgsConstructor
    @AllArgsConstructor
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
        private BigDecimal salesQty;

        /**
         * 金额
         */
        private BigDecimal amount;
    }

    @Getter
    @Setter
    public static class SalesInfoDTO {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 销量
         */
        private BigDecimal salesQty;

        /**
         * 断货数据是否从历史销量中排除,true是，false否
         */
        private Boolean isIgnoreOutOfStock;

        /**
         * denoisingType 去噪类型
         */
        private String denoisingType;

        /**
         * 原始销量
         */
        private Integer originalSalesQty;

        /**
         * 原始库存
         */
        private Integer originalInventoryQty;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriodSales {

        private TimePeriodEnum code;

        private BigDecimal qty;
    }


    @Getter
    @Setter
    public static class DeliverySuggestDTO {

        /**
         * 编码
         */
        private String code;
        /**
         * 创建类型（auto系统，manual人工）
         */
        private String createType;
        /**
         * 建议发货量
         */
        private Integer suggestDeliveryQty;
        /**
         * 建议发货日期
         */
        private LocalDate suggestDeliveryDate;
        /**
         * 物流方式,LogisticsMethodEnum枚举
         */
        private String logisticsMethod;
        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
        /**
         * 物流成本
         */
        private BigDecimal logisticsCost;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;


        public static DeliverySuggestDTO buildDeliverySuggestDTO(String code, CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult, String detailId, String createType) {
            DeliverySuggestDTO dto = new DeliverySuggestDTO();
            dto.setCode(code);
            dto.setCreateType(createType);
            dto.setLogisticsDays(logisticsResult.getLogisticsDays());
            dto.setLogisticsMethod(logisticsResult.getLogisticsMethod());
            dto.setSourceId(detailId);
            dto.setSourceType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class PurchaseSuggestDTO {
        /**
         * 编码
         */
        private String code;
        /**
         * 创建类型（auto系统，manual人工）
         */
        private String createType;
        /**
         * 建议采购量
         */
        private Integer suggestPurchaseQty;
        /**
         * 建议采购日期
         */
        private LocalDate suggestPurchaseDate;
        /**
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;
        /**
         * 预计入库日期
         */
        private LocalDate estimateInstockDate;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
        /**
         * 采购成本
         */
        private BigDecimal purchaseCost;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;

        public static PurchaseSuggestDTO buildPurchaseSuggestDTO(String code, CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult, String detailId, String createType) {
            PurchaseSuggestDTO dto = new PurchaseSuggestDTO();
            dto.setCode(code);
            dto.setCreateType(createType);
            dto.setLogisticsMethod(logisticsResult.getLogisticsMethod());
            dto.setLogisticsDays(logisticsResult.getLogisticsDays());
            dto.setSourceId(detailId);
            dto.setSourceType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            return dto;
        }
    }


    @Getter
    @Setter
    public static class SalesEstimateDTO {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 销量
         */
        private BigDecimal salesQty;

        /**
         * 所属月份
         */
        private String month;
    }

    @Getter
    @Setter
    public static class EstimatedDeliveryDetailDTO {
        /**
         * 状态
         */
        private String status;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 业务类型 FBA/海外仓
         */
        private String type;

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
    }


    @Getter
    @Setter
    public static class EstimatedPurchaseDetailDTO {
        /**
         * 状态
         */
        private String status;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计入库日期
         */
        private LocalDate estimatedPutAwayDate;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 业务类型 本地
         */
        private String type;

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
    }


    @Getter
    @Setter
    public static class LocalInTransitDetailDTO {

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 计算版本  所有子表加   根据单号生成规则
         */
        private String calcVersion;

        /**
         * 来源类型
         */
        private String sourceType;
    }

    @Getter
    @Setter
    public static class OverseasInTransitDetailDTO {

        /**
         * 发货单id
         */
        private String deliveryPlanId;

        /**
         * 发货单code
         */
        private String deliveryPlanCode;

        /**
         * 状态
         */
        private String status;

        /**
         * 发货日期
         */
        private LocalDate deliveryDate;

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
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

    }
}
