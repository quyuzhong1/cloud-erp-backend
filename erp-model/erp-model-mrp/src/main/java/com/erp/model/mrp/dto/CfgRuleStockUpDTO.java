package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.CfgRuleSafeDaysEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

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
         * 平台安全天数（天）
         */
        private Integer platformSafeDays;
        /**
         * 海外仓安全天数（天）
         */
        private Integer overseasSafeDays;

        /**
         * 常规品备货系数
         */
        private BigDecimal stockingRatio;

        /**
         * 新品备货系数
         */
        private BigDecimal newStockingRatio;

        /**
         * 平台类型
         */
        private String platform;

        /**
         * 关联id
         */
        private String refId;

        /**
         * 关联类型
         */
        private String refType;
        /**
         * 是否同常规品设置
         */
        private Boolean isCfgSame;

        /**
         * 常规品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> newStockingRatioList;

        /**
         * 平台安全天数（天）更多
         */
        private List<CfgRuleSafeDaysDTO.ViewDTO> platformSafeDaysList;
        /**
         * 海外仓安全天数（天）更多
         */
        private List<CfgRuleSafeDaysDTO.ViewDTO> overseasSafeDaysList;

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
         * 常规品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> newStockingRatioList;

        /**
         * 平台安全天数（天）更多
         */
        @Valid
        private List<CfgRuleSafeDaysDTO.UpdateDTO> platformSafeDaysList;
        /**
         * 海外仓安全天数（天）更多
         */
        @Valid
        private List<CfgRuleSafeDaysDTO.UpdateDTO> overseasSafeDaysList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 平台安全天数（天）
         */
        @Min(value = 0, message = "平台仓安全天数（天）最小值为0")
        @Max(value = 365, message = "平台仓安全天数（天）最大值为365")
        private Integer platformSafeDays;

        /**
         * 海外仓安全天数（天）
         */
        @Min(value = 0, message = "海外仓安全天数（天）最小值为0")
        @Max(value = 365, message = "海外仓安全天数（天）最大值为365")
        private Integer overseasSafeDays;

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
         * 平台类型
         */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 32, message = "平台类型最大长度不能超过32位")
        private String platform;

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
         * 是否同常规品设置
         */
        private Boolean isCfgSame;
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
         * 平台
         *
         */
        private String platform;
        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;

        /**
         * sku类型
         */
        private String skuType;

        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 备货配置
         */
        private List<CfgRuleStockUpEntity> cfgRuleStockUpList;
        /**
         * 备货系数配置
         */
        private List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList;

        /**
         * 安全天数配置
         */
        private List<CfgRuleSafeDaysEntity> cfgRuleSafeDaysList;

        public static StrategyDTO buildStrategyDTO(ReplenishmentResultDTO resultDTO, List<CfgRuleStockUpEntity> cfgRuleStockUpList,
                                                   List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList,
                                                   List<CfgRuleSafeDaysEntity> cfgRuleSafeDaysList) {
            StrategyDTO dto = new StrategyDTO();
            dto.setRefId(resultDTO.getReplenishment().getId());
            dto.setSkuType(resultDTO.getReplenishmentDetail().getSkuType());
            dto.setPlatform(resultDTO.getReplenishment().getPlatform());
            dto.setShopId(resultDTO.getReplenishment().getShopId());
            dto.setPlatformType(resultDTO.getReplenishment().getPlatformType());
            dto.setCfgRuleStockUpList(cfgRuleStockUpList);
            dto.setCfgRuleStockingRatioList(cfgRuleStockingRatioList);
            dto.setCfgRuleSafeDaysList(cfgRuleSafeDaysList);
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
         * 安全天数（天）
         */
        private Integer safeDays;
        /**
         * 默认备货系数
         */
        private BigDecimal stockingRatio;
        /**
         * sku备货系数
         */
        private BigDecimal refStockingRatio;
        /**
         * 平台类型
         */
        private String platform;
        /**
         * 关联id
         */
        private String refId;
        /**
         * 备货默认明细系数
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults;
        /**
         * 备货明细系数
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> refStockingRatioResults;


        public void buildStrategyResultDTO(StrategyDTO dto, CfgRuleStockUpEntity entity, CfgRuleStockUpEntity defaultStockUp, List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults
                , List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> refStockingRatioResults) {
            // 设置实体字段值
            this.setId(getOrDefault(entity, CfgRuleStockUpEntity::getId, defaultStockUp.getId()));
            // 设置默认/新备货比率
            this.setStockingRatio(CfgRuleStockingRatioTypeEnum.NEW.getCode().equals(dto.getSkuType()) ? defaultStockUp.getNewStockingRatio() : defaultStockUp.getStockingRatio());
            this.setRefStockingRatio(getOrDefault(entity, CfgRuleStockUpEntity::getStockingRatio, null));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType())) {
                this.setSafeDays(getOrDefault(entity, CfgRuleStockUpEntity::getPlatformSafeDays, getDefaultSafeDays(dto, defaultStockUp)));
            } else {
                this.setSafeDays(getOrDefault(entity, CfgRuleStockUpEntity::getOverseasSafeDays, getDefaultSafeDays(dto, defaultStockUp)));
            }
            // 设置平台类型和引用ID
            this.setPlatform(getOrDefault(entity, CfgRuleStockUpEntity::getPlatform, defaultStockUp.getPlatform()));
            this.setRefId(getOrDefault(entity, CfgRuleStockUpEntity::getRefId, ""));
            // 设置备货比率结果
            this.setStockingRatioResults(stockingRatioResults);
            this.setRefStockingRatioResults(refStockingRatioResults);
        }

        private Integer getDefaultSafeDays(StrategyDTO dto, CfgRuleStockUpEntity defaultStockUp) {
            return dto.getCfgRuleSafeDaysList()
                    .stream().filter(v -> v.getPlatformType().equals(dto.getPlatformType()))
                    .filter(v -> v.getShopIdJson().contains(dto.getShopId()))
                    .findFirst()
                    .map(CfgRuleSafeDaysEntity::getSafeDays)
                    .orElse(CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType()) ? defaultStockUp.getPlatformSafeDays() : defaultStockUp.getOverseasSafeDays());
        }

        // 提取默认值的辅助方法
        private <T> T getOrDefault(CfgRuleStockUpEntity entity, Function<CfgRuleStockUpEntity, T> getter, T defaultValue) {
            return (entity != null && getter.apply(entity) != null) ? getter.apply(entity) : defaultValue;
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