package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import lombok.*;
import org.springframework.util.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 备货（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Data
@NoArgsConstructor
public class CfgRuleStockUpDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

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
         * 常规品备货系数
         */
        private BigDecimal stockingRatio;

        /**
         * 新品备货系数
         */
        private BigDecimal newStockingRatio;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;

        /**
         * 关联id
         */
        private String refId;

        /**
         * 关联类型
         */
        private String refType;

        /**
         * 物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> cfgLogisticsList;

        /**
         * 常规品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> newStockingRatioList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class CustomUpdateDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = true;

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
         * 常规品备货系数
         */
        @Digits(integer = 12, fraction = 4, message = "常规品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stockingRatio;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32, message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;

        /**
         * 关联id
         */
        @Size(max = 19, message = "关联id最大长度不能超过19位")
        private String refId;

        /**
         * 关联类型
         */
        @Size(max = 32, message = "关联类型最大长度不能超过32位")
        private String refType;

        /**
         * 物流信息
         */
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> cfgLogisticsList;

        /**
         * 常规品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> newStockingRatioList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 物流信息
         */
        @NotEmpty(message = "物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> cfgLogisticsList;

        /**
         * 常规品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> newStockingRatioList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 采购审批天数（天）
         */
        @NotNull(message = "采购审批天数（天）不能为空")
        @Min(value = 0, message = "采购审批天数（天）最小值为0")
        @Max(value = 365, message = "采购审批天数（天）最大值为365")
        private Integer purchaseApproveDays;

        /**
         * 生产周期天数（天）
         */
        @NotNull(message = "生产周期天数（天）不能为空")
        @Min(value = 0, message = "生产周期天数（天）最小值为0")
        @Max(value = 365, message = "生产周期天数（天）最大值为365")
        private Integer productionDays;

        /**
         * 供应商发货天数（天）
         */
        @NotNull(message = "供应商发货天数（天）不能为空")
        @Min(value = 0, message = "供应商发货天数（天）最小值为0")
        @Max(value = 365, message = "供应商发货天数（天）最大值为365")
        private Integer supplierDeliveryDays;

        /**
         * 质检入库天数（天）
         */
        @NotNull(message = "质检入库天数（天）不能为空")
        @Min(value = 0, message = "质检入库天数（天）最小值为0")
        @Max(value = 365, message = "质检入库天数（天）最大值为365")
        private Integer qcDays;

        /**
         * 采购频率天数（天）
         */
        @NotNull(message = "采购频率天数（天）不能为空")
        @Min(value = 0, message = "采购频率天数（天）最小值为0")
        @Max(value = 365, message = "采购频率天数（天）最大值为365")
        private Integer purchaseCycleDays;

        /**
         * 安全天数（天）
         */
        @NotNull(message = "安全天数（天）不能为空")
        @Min(value = 0, message = "安全天数（天）最小值为0")
        @Max(value = 365, message = "安全天数（天）最大值为365")
        private Integer safeDays;

        /**
         * 入库天数（天）
         */
        @NotNull(message = "入库天数（天）不能为空")
        @Min(value = 0, message = "入库天数（天）最小值为0")
        @Max(value = 365, message = "入库天数（天）最大值为365")
        private Integer instockDays;

        /**
         * 常规品备货系数
         */
        @NotNull(message = "常规品备货系数不能为空")
        @Digits(integer = 12, fraction = 4, message = "常规品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stockingRatio;

        /**
         * 新品备货系数
         */
        @NotNull(message = "新品备货系数不能为空")
        @Digits(integer = 12, fraction = 4, message = "新品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal newStockingRatio;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32, message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;

        /**
         * 关联id
         */
        @Size(max = 19, message = "关联id最大长度不能超过19位")
        private String refId;

        /**
         * 关联类型
         */
        @Size(max = 32, message = "关联类型最大长度不能超过32位")
        private String refType;

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategyDTO {

        /**
         * 补货建议id
         */
        private String refId;
        /**
         * 平台类型
         *
         * @see CfgRulePlatformTypeEnum
         */
        private String platformType;
        /**
         * sku类型
         *
         * @see CfgRuleStockingRatioTypeEnum
         */
        private String skuType;
        /**
         * 区域
         */
        private String area;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 海外仓id
         */
        private List<String> warehouseId;
        /**
         * 默认备货配置
         */
        private CfgRuleStockUpEntity defaultStockUp;
        /**
         * 默认备货系数配置
         */
        private List<CfgRuleStockingRatioEntity> defaultStockingRatio;
        /**
         * 默认物流配置
         */
        private List<CfgRuleLogisticsEntity> defaultLogistics;
        /**
         * 默认物流明细配置
         */
        private List<CfgRuleLogisticsDetailEntity> logisticsDetails;

        public static StrategyDTO buildStrategyDTO(ReplenishmentResultDTO resultDTO, CfgRuleStockUpEntity defaultStockUp, List<CfgRuleStockingRatioEntity> defaultStockingRatio,
                                                   List<CfgRuleLogisticsEntity> defaultLogistics, List<CfgRuleLogisticsDetailEntity> logisticsDetails) {
            ReplenishmentResultDTO.BasicDTO entity = resultDTO.getReplenishment();
            StrategyDTO dto = new StrategyDTO();
            dto.setRefId(entity.getId());
            dto.setPlatformType(entity.getPlatformType());
            dto.setSkuType(resultDTO.getReplenishmentDetail().getSkuType());
            dto.setArea(entity.getArea());
            dto.setShopId(entity.getShopId());
            dto.setDefaultStockUp(defaultStockUp);
            dto.setDefaultStockingRatio(defaultStockingRatio);
            dto.setDefaultLogistics(defaultLogistics);
            dto.setLogisticsDetails(logisticsDetails);
            dto.setWarehouseId(resultDTO.getOverseasWarehouseId());
            return dto;
        }

    }

    @Getter
    @Setter
    public static class StrategyResultDTO {
        /**
         * 备货id
         */
        private String id;
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
         * 常规品备货系数
         */
        private BigDecimal stockingRatio;
        /**
         * 新品备货系数
         */
        private BigDecimal newStockingRatio;
        /**
         * sku备货系数
         */
        private BigDecimal refStockingRatio;
        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;
        /**
         * 关联id
         */
        private String refId;
        /**
         * 物流时效
         */
        private CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult;
        /**
         * 物流最大时效
         */
        private CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMaxResult;
        /**
         * 物流最小时效
         */
        private CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMinResult;
        /**
         * 备货默认明细系数
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults;
        /**
         * 备货明细系数
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> refStockingRatioResults;


        public void buildStrategyResultDTO(CfgRuleStockUpEntity entity, CfgRuleStockUpEntity defaultStockUp, List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults,
                                           CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult, CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMaxResult,
                                           CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMinResult,List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> refStockingRatioResults
        ) {
            this.setId(Optional.ofNullable(entity).orElse(defaultStockUp).getId());
            this.setPurchaseApproveDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getPurchaseApproveDays())) ? defaultStockUp.getPurchaseApproveDays() : entity.getPurchaseApproveDays());
            this.setProductionDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getProductionDays())) ? defaultStockUp.getProductionDays() : entity.getProductionDays());
            this.setSupplierDeliveryDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getSupplierDeliveryDays())) ? defaultStockUp.getSupplierDeliveryDays() : entity.getSupplierDeliveryDays());
            this.setQcDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getQcDays())) ? defaultStockUp.getQcDays() : entity.getQcDays());
            this.setPurchaseCycleDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getPurchaseCycleDays())) ? defaultStockUp.getPurchaseCycleDays() : entity.getPurchaseCycleDays());
            this.setSafeDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getSafeDays())) ? defaultStockUp.getSafeDays() : entity.getSafeDays());
            this.setInstockDays((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getInstockDays())) ? defaultStockUp.getInstockDays() : entity.getInstockDays());
            this.setStockingRatio(defaultStockUp.getStockingRatio());
            this.setNewStockingRatio(defaultStockUp.getNewStockingRatio());
            this.setRefStockingRatio(ObjectUtils.isEmpty(entity) ? null : entity.getStockingRatio());
            this.setPlatformType((ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getPlatformType())) ? defaultStockUp.getPlatformType() : entity.getPlatformType());
            this.setRefId((ObjectUtils.isEmpty(entity)) ? "" : entity.getRefId());
            this.setLogisticsResult(logisticsResult);
            this.setLogisticsMinResult(logisticsMinResult);
            this.setLogisticsMaxResult(logisticsMaxResult);
            this.setStockingRatioResults(stockingRatioResults);
            this.setRefStockingRatioResults(refStockingRatioResults);
        }
    }

    @Data
    @NoArgsConstructor
    public static class StockUpExportDTO {
        /**
         * 平台
         */
        private String platform;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * 采购审批（天）
         */
        private Integer purchaseApproveDays;

        /**
         * 生产周期（天）
         */
        private Integer productionDays;

        /**
         * 供应商发货（天）
         */
        private Integer supplierDeliveryDays;

        /**
         * 质检入库（天）
         */
        private Integer qcDays;

        /**
         * 采购频率（天）
         */
        private Integer purchaseCycleDays;

        /**
         * 物流时效（空运）
         */
        private Integer oneLogisticsDays;

        /**
         * 发货频率（空运)
         */
        private Integer oneLogisticsCycleDays;

        /**
         * 优先级（空运)
         */
        private Integer oneIndex;

        /**
         * 物流时效（快递）
         */
        private Integer twoLogisticsDays;

        /**
         * 发货频率（快递)
         */
        private Integer twoLogisticsCycleDays;

        /**
         * 优先级（快递)
         */
        private Integer twoIndex;

        /**
         * 物流时效（海运散装）
         */
        private Integer threeLogisticsDays;

        /**
         * 发货频率（海运散装)
         */
        private Integer threeLogisticsCycleDays;

        /**
         * 优先级（海运散装)
         */
        private Integer threeIndex;

        /**
         * 物流时效（海运整柜）
         */
        private Integer fourLogisticsDays;

        /**
         * 发货频率（海运整柜)
         */
        private Integer fourLogisticsCycleDays;

        /**
         * 优先级（海运整柜)
         */
        private Integer fourIndex;

        /**
         * 物流时效（铁运散装）
         */
        private Integer fiveLogisticsDays;

        /**
         * 发货频率（铁运散装)
         */
        private Integer fiveLogisticsCycleDays;

        /**
         * 优先级（铁运散装)
         */
        private Integer fiveIndex;

        /**
         * 物流时效（铁运整柜）
         */
        private Integer sixLogisticsDays;

        /**
         * 发货频率（铁运整柜)
         */
        private Integer sixLogisticsCycleDays;

        /**
         * 优先级（铁运整柜)
         */
        private Integer sixIndex;

        /**
         * 安全天数
         */
        private Integer safeDays;

        /**
         * 默认备货系数
         */
        private BigDecimal stockingRatio;

    }

}