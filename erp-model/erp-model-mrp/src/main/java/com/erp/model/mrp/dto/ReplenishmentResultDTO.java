package com.erp.model.mrp.dto;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.RecentTimePeriodEnum;
import com.erp.model.mrp.enums.TimePeriodEnum;
import com.erp.model.mrp.vo.ReplenishmentSuggestionVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
     * 本地可用库存明细
     */
    private List<ReplenishmentInventoryDetailDTO> localUsableDetail;
    /**
     * 本地在途明细
     */
    private List<LocalInTransitDetailDTO> localInTransitDetails;
    /**
     * 本地在途库存明细
     */
    private List<ReplenishmentInventoryDetailDTO> localInTransitDetail;
    /**
     * 本地采购明细
     */
    private List<EstimatedPurchaseDetailDTO> localPurchaseDetails;
    /**
     * 本地采购库存明细
     */
    private List<ReplenishmentInventoryDetailDTO> localPurchaseDetail;

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
    private List<TimePeriodSalesDTO> timePeriodSales;
    /**
     * 分时段日均销量
     */
    private List<TimePeriodSalesDTO> avgTimePeriodSales;

    /**
     * 销量预估
     */
    private List<SalesEstimateDTO> salesEstimates;

    /**
     * 分时段销量预估
     */
    private List<TimePeriodSalesEstimateDTO> timePeriodSalesEstimates;

    /**
     * 分时段日均销量预估
     */
    private List<TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates;

    /**
     * 建议发货
     */
    private List<DeliverySuggestDTO> deliverySuggests;

    /**
     * 建议采购
     */
    private List<PurchaseSuggestDTO> purchaseSuggests;

    /**
     * 最近建议明细
     */
    private List<RecentSuggestionDTO> recentSuggestions;

    /**
     * 采购单价
     */
    private BigDecimal purchasePrice;

    /**
     * 销售价
     */
    private BigDecimal salesPrice;

    /**
     * 本地仓id
     */
    private List<String> localWarehouseId;

    /**
     * 海外仓id
     */
    private List<String> overseasWarehouseId;

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
         * fba店铺
         */
        private String fbaWarehouseId;

        /**
         * 平台
         */
        private String platform;

        /**
         * 区域
         */
        private String area;

        public static BasicDTO buildBasicDTO(ReplenishmentSuggestionEntity entity) {
            BasicDTO dto = new BasicDTO();
            dto.setId(entity.getId());
            dto.setPlatformType(entity.getPlatformType());
            dto.setSkuId(entity.getSkuId());
            dto.setSkuNo(entity.getSkuNo());
            dto.setCountry(entity.getCountry());
            dto.setShopId(entity.getShopId());
            dto.setPlatform(entity.getPlatform());
            dto.setFbaWarehouseId(entity.getFbaWarehouseId());
            dto.setArea(entity.getArea());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class DetailDTO {

        /**
         * 明细id
         */
        private String detailId;
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
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流方式
         */
        private String logisticsMaxMethod;

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
         * 在途配置
         */
        private String cfgFbaInTransit;

        /**
         * 计算版本  所有子表加   根据单号生成规则
         */
        private String calcVersion;

        /**
         * 计算日期
         */
        private String calcDate;


        public static DetailDTO buildDetail(ReplenishmentSuggestionDetailEntity entity) {
            DetailDTO dto = new DetailDTO();
            dto.setDetailId(entity.getId());
            dto.setMainId(entity.getMainId());
            dto.setCalcDate(entity.getCalcDate());
            dto.setCalcVersion(entity.getCalcVersion());
            dto.setSkuType(entity.getSkuType());
            return dto;
        }

        public static DetailDTO buildDetailNotId(ReplenishmentSuggestionDetailEntity entity) {
            DetailDTO dto = new DetailDTO();
            dto.setDetailId(IdWorker.getIdStr());
            dto.setMainId(entity.getMainId());
            dto.setSkuType(entity.getSkuType());
            return dto;
        }

        public static ReplenishmentSuggestionDetailEntity buildReplenishmentSuggestionDetail(DetailDTO dto, CfgRuleStrategyDTO cfgRuleStrategy, List<TimePeriodSalesEstimateDTO> timePeriodSalesEstimates,
                                                                                             List<TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates, List<TimePeriodSalesDTO> timePeriodSales,
                                                                                             List<TimePeriodSalesDTO> avgTimePeriodSales, BigDecimal purchasePrice, BigDecimal salesPrice) {

            ReplenishmentSuggestionDetailEntity detail = new ReplenishmentSuggestionDetailEntity();
            detail.setId(dto.getDetailId());
            detail.setMainId(dto.getMainId());
            detail.setSkuType(dto.getSkuType());
            detail.setFbaUsableQty(dto.getFbaUsableQty());
            detail.setFbaInTransitQty(dto.getFbaInTransitQty());
            detail.setFbaPlanDeliveryQty(dto.getFbaPlanDeliveryQty());
            detail.setOverseasUsableQty(dto.getOverseasUsableQty());
            detail.setOverseasInTransitQty(dto.getOverseasInTransitQty());
            detail.setOverseasPlanDeliveryQty(dto.getOverseasPlanDeliveryQty());
            detail.setLocalUsableQty(dto.getLocalUsableQty());
            detail.setLocalInTransitQty(dto.getLocalInTransitQty());
            detail.setLocalPlanPurchaseQty(dto.getLocalPlanPurchaseQty());
            detail.setTotalInventoryQty(dto.getTotalInventoryQty());
            if (!CollectionUtils.isEmpty(timePeriodSales)) {
                detail.setSalesQty(JSONUtil.parseArray(timePeriodSales.stream().map(v -> new ReplenishmentSuggestionVO.SalesVO(v.getCode().getName(), v.getQty()))
                        .collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(avgTimePeriodSales)) {
                detail.setAvgSalesQty(JSONUtil.parseArray(avgTimePeriodSales.stream().map(v -> new ReplenishmentSuggestionVO.SalesVO(v.getCode().getName(), v.getQty()))
                        .collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(timePeriodSalesEstimates)) {
                detail.setSalesEstimateQty(JSONUtil.parseArray(timePeriodSalesEstimates.stream().map(v -> new ReplenishmentSuggestionVO.SalesVO(RecentTimePeriodEnum.getNameByCode(v.getCode(), false, dto.getCalcDate()), v.getQty()))
                        .collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(avgTimePeriodSalesEstimates)) {
                detail.setAvgSalesEstimateQty(JSONUtil.parseArray(avgTimePeriodSalesEstimates.stream().map(v -> new ReplenishmentSuggestionVO.SalesVO(RecentTimePeriodEnum.getNameByCode(v.getCode(), true, dto.getCalcDate()), v.getQty()))
                        .collect(Collectors.toList())));
            }
            detail.setPurchaseApproveDays(dto.getPurchaseApproveDays());
            detail.setProductionDays(dto.getProductionDays());
            detail.setSupplierDeliveryDays(dto.getSupplierDeliveryDays());
            detail.setQcDays(dto.getQcDays());
            detail.setPurchaseCycleDays(dto.getPurchaseCycleDays());
            detail.setDeliveryMinDays(dto.getDeliveryMinDays());
            detail.setDeliveryDefaultDays(dto.getDeliveryDefaultDays());
            detail.setDeliveryMaxDays(dto.getDeliveryMaxDays());
            detail.setLogisticsCycleDays(dto.getLogisticsCycleDays());
            detail.setLogisticsMinCycleDays(dto.getLogisticsMinCycleDays());
            detail.setLogisticsMaxCycleDays(dto.getLogisticsMaxCycleDays());
            detail.setLogisticsMinMethod(dto.getLogisticsMinMethod());
            detail.setLogisticsMethod(dto.getLogisticsMethod());
            detail.setLogisticsMaxMethod(dto.getLogisticsMaxMethod());
            detail.setStockUpMinDays(dto.getStockUpMinDays());
            detail.setStockUpDefaultDays(dto.getStockUpDefaultDays());
            detail.setStockUpMaxDays(dto.getStockUpMaxDays());
            detail.setSafeDays(dto.getSafeDays());
            detail.setInstockDays(dto.getInstockDays());
            detail.setFbaSellableDays(dto.getFbaSellableDays());
            detail.setSellableDays(dto.getSellableDays());
            detail.setOverseasSellableDays(dto.getOverseasSellableDays());
            detail.setLocalSellableDays(dto.getLocalSellableDays());
            detail.setTotalSellableDays(dto.getTotalSellableDays());
            detail.setCfgFbaInTransit(dto.getCfgFbaInTransit());
            detail.setCalcVersion(dto.getCalcVersion());
            detail.setCalcDate(dto.getCalcDate());
            detail.setCfgRule(JSON.toJSONString(cfgRuleStrategy));
            detail.setPurchasePrice(purchasePrice);
            detail.setSalesPrice(salesPrice);
            return detail;
        }

        public static ReplenishmentSuggestionDetailEntity buildNewDetail(DetailDTO detailDTO) {
            ReplenishmentSuggestionDetailEntity entity = new ReplenishmentSuggestionDetailEntity();
            entity.setId(detailDTO.getDetailId());
            entity.setCalcVersion(detailDTO.getCalcVersion());
            entity.setCalcDate(detailDTO.getCalcDate());
            entity.setMainId(detailDTO.getMainId());
            entity.setSkuType(detailDTO.getSkuType());
            return entity;
        }
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

        public static FbaInTransitDetailEntity buildFbaInTransitDetail(FbaInTransitDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            FbaInTransitDetailEntity entity = new FbaInTransitDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setSourceType(dto.getSourceType());
            entity.setStatus(dto.getStatus());
            entity.setDeliveryDate(dto.getDeliveryDate());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setDeclareQty(dto.getDeclareQty());
            entity.setDeliveryQty(dto.getDeliveryQty());
            entity.setReceiveQty(dto.getReceiveQty());
            entity.setInTransitQty(dto.getInTransitQty());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
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

        public static RptOutOfStockEntity buildRptOutOfStock(RptOutOfStockDTO dto, String replenishmentDetailId, String calcVersion) {
            RptOutOfStockEntity entity = new RptOutOfStockEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setDate(dto.getDate());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setSalesQty(dto.getSalesQty());
            entity.setAmount(dto.getAmount());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
    }

    @Getter
    @Setter
    public static class SalesInfoDTO {
        /**
         * id
         */
        private String id;
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

        public static SalesInfoDTO buildSalesInfoDTO(SalesInfoEntity entity) {
            SalesInfoDTO dto = new SalesInfoDTO();
            dto.setId(entity.getId());
            dto.setDate(entity.getDate());
            dto.setOriginalSalesQty(entity.getOriginalSalesQty());
            dto.setOriginalInventoryQty(entity.getOriginalInventoryQty());
            return dto;
        }

        public static SalesInfoEntity buildSalesInfo(SalesInfoDTO dto, String replenishmentDetailId, String calcVersion) {
            SalesInfoEntity entity = new SalesInfoEntity();
            entity.setId(dto.getId());
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setDate(dto.getDate());
            entity.setSalesQty(dto.getSalesQty());
            entity.setIsIgnoreOutOfStock(dto.getIsIgnoreOutOfStock());
            entity.setSalesQtyType(dto.getDenoisingType());
            entity.setOriginalSalesQty(dto.getOriginalSalesQty());
            entity.setOriginalInventoryQty(dto.getOriginalInventoryQty());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriodSalesDTO {

        private TimePeriodEnum code;

        private BigDecimal qty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriodSalesEstimateDTO {

        private RecentTimePeriodEnum code;

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

        public static DeliverySuggestEntity buildDeliverySuggest(DeliverySuggestDTO dto) {
            DeliverySuggestEntity entity = new DeliverySuggestEntity();
            entity.setCode(dto.getCode());
            entity.setCreateType(dto.getCreateType());
            entity.setSuggestDeliveryQty(dto.getSuggestDeliveryQty());
            entity.setSuggestDeliveryDate(dto.getSuggestDeliveryDate());
            entity.setLogisticsMethod(dto.getLogisticsMethod());
            entity.setLogisticsDays(dto.getLogisticsDays());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setLogisticsCost(dto.getLogisticsCost());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceType(dto.getSourceType());
            return entity;
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

        public static PurchaseSuggestEntity buildPurchaseSuggest(PurchaseSuggestDTO dto) {
            PurchaseSuggestEntity entity = new PurchaseSuggestEntity();
            entity.setCode(dto.getCode());
            entity.setCreateType(dto.getCreateType());
            entity.setSuggestPurchaseQty(dto.getSuggestPurchaseQty());
            entity.setSuggestPurchaseDate(dto.getSuggestPurchaseDate());
            entity.setLogisticsMethod(dto.getLogisticsMethod());
            entity.setLogisticsDays(dto.getLogisticsDays());
            entity.setEstimateInstockDate(dto.getEstimateInstockDate());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setPurchaseCost(dto.getPurchaseCost());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceType(dto.getSourceType());
            return entity;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
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

        public static SalesEstimateEntity buildSalesEstimate(SalesEstimateDTO dto, String replenishmentDetailId, String calcVersion) {
            SalesEstimateEntity entity = new SalesEstimateEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setDate(dto.getDate());
            entity.setSalesQty(dto.getSalesQty());
            entity.setMonth(dto.getMonth());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
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

        public static EstimatedDeliveryDetailEntity buildEstimatedDeliveryDetail(EstimatedDeliveryDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            EstimatedDeliveryDetailEntity entity = new EstimatedDeliveryDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setStatus(dto.getStatus());
            entity.setQty(dto.getQty());
            entity.setType(dto.getType());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setSourceType(dto.getSourceType());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
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
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 采购订单生成状态
         */
        private String createPoType;
        /**
         * 明细id
         */
        private String detailId;

        public static EstimatedPurchaseDetailEntity buildEstimatedPurchaseDetail(EstimatedPurchaseDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            EstimatedPurchaseDetailEntity entity = new EstimatedPurchaseDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setStatus(dto.getStatus());
            entity.setQty(dto.getQty());
            entity.setPlanArrivalDate(dto.getEstimatedPutAwayDate());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setType(dto.getType());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setSourceType(dto.getSourceType());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
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
         * 预计入库日期
         */
        private LocalDate estimatedPutAwayDate;

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
        /**
         * 仓库id
         */
        private String warehouseId;

        public static LocalInTransitDetailEntity buildLocalInTransitDetail(LocalInTransitDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            LocalInTransitDetailEntity entity = new LocalInTransitDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setQty(dto.getQty());
            entity.setQty(dto.getQty());
            entity.setPlanArrivalDate(dto.getEstimatedPutAwayDate());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setSourceType(dto.getSourceType());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
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

        public static OverseasInTransitDetailEntity buildOverseasInTransitDetail(OverseasInTransitDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            OverseasInTransitDetailEntity entity = new OverseasInTransitDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setDeliveryPlanId(dto.getDeliveryPlanId());
            entity.setDeliveryPlanCode(dto.getDeliveryPlanCode());
            entity.setStatus(dto.getStatus());
            entity.setDeliveryDate(dto.getDeliveryDate());
            entity.setDeliveryQty(dto.getDeliveryQty());
            entity.setReceiveQty(dto.getReceiveQty());
            entity.setInTransitQty(dto.getInTransitQty());
            entity.setEstimateSalesDate(dto.getEstimateSalesDate());
            entity.setCalcVersion(calcVersion);
            return entity;
        }

    }


    @Getter
    @Setter
    public static class ReplenishmentInventoryDetailDTO {
        /**
         * 类型   海外仓可用/海外仓在途/预计发货/本地仓可用/本地仓在途/预计采购
         */
        private String inventoryType;

        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 仓库类型，local本地，overseas海外
         */
        private String warehouseType;

        /**
         * 关联店铺类型，platform按平台，shop按店铺
         */
        private String channelType;

        /**
         * 店铺id的json
         */
        private JSONArray channelIdJson;

        /**
         * 库存分配类型
         */
        private String inventoryAllocateType;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 店铺明细
         */
        private List<ShopInventoryDetailDTO> shopInventoryDetails;


        public static ReplenishmentInventoryDetailEntity buildReplenishmentInventoryDetail(ReplenishmentInventoryDetailDTO dto, String replenishmentDetailId, String calcVersion) {
            ReplenishmentInventoryDetailEntity entity = new ReplenishmentInventoryDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setInventoryType(dto.getInventoryType());
            entity.setWarehouseId(dto.getWarehouseId());
            entity.setVirtualWarehouseId(dto.getVirtualWarehouseId());
            entity.setWarehouseType(dto.getWarehouseType());
            entity.setChannelType(dto.getChannelType());
            entity.setChannelIdJson(dto.getChannelIdJson());
            entity.setInventoryAllocateType(dto.getInventoryAllocateType());
            entity.setTotalQty(dto.getTotalQty());
            entity.setCalcVersion(calcVersion);
            return entity;
        }

        public static ReplenishmentInventoryDetailDTO buildReplenishmentInventoryDetailDTO(String inventoryType, CfgRuleWarehouseDTO.StrategyDetailResultDTO result, Integer totalQty, List<ShopInventoryDetailDTO> shopInventoryDetails) {
            ReplenishmentInventoryDetailDTO dto = new ReplenishmentInventoryDetailDTO();
            dto.setInventoryType(inventoryType);
            dto.setWarehouseId(result.getWarehouseId());
            dto.setVirtualWarehouseId(result.getVirtualWarehouseId());
            dto.setWarehouseType(result.getWarehouseType());
            dto.setChannelType(result.getChannelType());
            dto.setChannelIdJson(result.getChannelIdJson());
            dto.setInventoryAllocateType(result.getInventoryAllocateType());
            dto.setTotalQty(totalQty);
            dto.setShopInventoryDetails(shopInventoryDetails);
            return dto;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInventoryDetailDTO {
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 数量
         */
        private BigDecimal qty;

        public static ShopInventoryDetailEntity buildShopInventoryDetail(ShopInventoryDetailDTO dto, String mainId, String calcVersion) {
            ShopInventoryDetailEntity entity = new ShopInventoryDetailEntity();
            entity.setMainId(mainId);
            entity.setShopId(dto.getShopId());
            entity.setQty(dto.getQty());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
    }


    @Getter
    @Setter
    public static class SalesInfoAllDTO {
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * sku id
         */
        private String skuId;
        /**
         * 店铺id
         */
        private String shopId;
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
    public static class RecentSuggestionDTO {
        /**
         * 类型
         */
        private String type;

        /**
         * 天数
         */
        private Integer days;

        /**
         * 补货建议标识类型
         */
        private String markType;

        /**
         * 建议数量
         */
        private Integer qty;

        /**
         * 日期
         */
        private LocalDate date;

        public static RecentSuggestionDTO buildRecentSuggestion(String type, Integer days, String markType, Integer qty, LocalDate date) {
            RecentSuggestionDTO dto = new RecentSuggestionDTO();
            dto.setType(type);
            dto.setQty(qty);
            dto.setDate(date);
            dto.setDays(days);
            dto.setMarkType(markType);
            return dto;
        }

        public static RecentSuggestionDetailEntity buildRecentSuggestionEntity(RecentSuggestionDTO dto, String replenishmentDetailId, String calcVersion) {

            RecentSuggestionDetailEntity entity = new RecentSuggestionDetailEntity();
            entity.setReplenishmentDetailId(replenishmentDetailId);
            entity.setType(dto.getType());
            entity.setDays(dto.getDays());
            entity.setMarkType(dto.getMarkType());
            entity.setQty(dto.getQty());
            entity.setDate(dto.getDate());
            entity.setCalcVersion(calcVersion);
            return entity;
        }
    }
}
