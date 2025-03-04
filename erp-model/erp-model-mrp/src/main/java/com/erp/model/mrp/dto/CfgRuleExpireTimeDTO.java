package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.entity.CfgRuleOverseasInstockDaysEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class CfgRuleExpireTimeDTO {
    @Getter
    @Setter
    public static class UpdateDTO {

        private String id;
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
         * 平台入库天数（天）
         */
        @NotNull(message = "平台入库天数（天）不能为空")
        @Min(value = 0, message = "平台入库天数（天）最小值为0")
        @Max(value = 365, message = "平台入库天数（天）最大值为365")
        private Integer platformInstockDays;

        /**
         * 海外仓入库天数（天）
         */
        @NotNull(message = "海外仓入库天数（天）不能为空")
        @Min(value = 0, message = "海外仓入库天数（天）最小值为0")
        @Max(value = 365, message = "海外仓入库天数（天）最大值为365")
        private Integer overseasInstockDays;

        /**
         * 海外仓入库天数明细
         */
        @Valid
        private List<CfgRuleOverseasInstockDaysDTO.UpdateDTO> overseasInstockDaysList;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 是否是批量
         */
        private Boolean isBatch = false;

        /**
         * 物流信息
         */
        @NotEmpty(message = "fba物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> platformCfgLogisticsList;

        /**
         * 物流信息
         */
        @NotEmpty(message = "海外仓物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> overseasCfgLogisticsList;


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
         * 平台入库天数（天）
         */
        private Integer platformInstockDays;

        /**
         * 海外仓入库天数（天）
         */
        private Integer overseasInstockDays;

        /**
         * 海外仓入库天数明细
         */
        private List<CfgRuleOverseasInstockDaysDTO.ViewDTO> overseasInstockDaysList;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 平台仓物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> platformCfgLogisticsList;

        /**
         * 海外仓物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> overseasCfgLogisticsList;

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
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;
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
         * 时效配置
         */
        private List<CfgRuleExpireTimeEntity> cfgRuleExpireTimeList;
        /**
         * 物流配置
         */
        private List<CfgRuleLogisticsEntity> cfgRuleLogisticsList;
        /**
         * 默认物流明细配置
         */
        private List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList;
        /**
         * 海外仓入库天数明细
         */
        private List<CfgRuleOverseasInstockDaysEntity> cfgRuleOverseasInStockDaysList;



        public static CfgRuleExpireTimeDTO.StrategyDTO buildStrategyDTO(ReplenishmentResultDTO resultDTO, List<CfgRuleExpireTimeEntity> cfgRuleExpireTimeList, List<CfgRuleLogisticsEntity> cfgRuleLogisticsList,
                                                                     List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList, List<CfgRuleOverseasInstockDaysEntity> cfgRuleOverseasInStockDaysList) {
            ReplenishmentResultDTO.BasicDTO entity = resultDTO.getReplenishment();
            CfgRuleExpireTimeDTO.StrategyDTO dto = new CfgRuleExpireTimeDTO.StrategyDTO();
            dto.setRefId(entity.getId());
            dto.setPlatformType(entity.getPlatformType());
            dto.setArea(entity.getArea());
            dto.setShopId(entity.getShopId());
            dto.setCfgRuleExpireTimeList(cfgRuleExpireTimeList);
            dto.setCfgRuleLogisticsList(cfgRuleLogisticsList);
            dto.setCfgRuleLogisticsDetailList(cfgRuleLogisticsDetailList);
            dto.setCfgRuleOverseasInStockDaysList(cfgRuleOverseasInStockDaysList);
            dto.setWarehouseId(resultDTO.getOverseasWarehouseId());
            return dto;
        }

    }

    @Getter
    @Setter
    public static class StrategyResultDTO {
        /**
         * 时效id
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
         * 入库天数（天）
         */
        private Integer instockDays;
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

        public void buildStrategyResultDTO(String platformType, CfgRuleExpireTimeEntity entity, CfgRuleExpireTimeEntity defaultExpireTime,
                                           CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult, CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMaxResult,
                                           CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMinResult
        ) {
            // 设置实体字段值
            this.setId(getOrDefault(entity, CfgRuleExpireTimeEntity::getId, defaultExpireTime.getId()));
            this.setPurchaseApproveDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getPurchaseApproveDays, defaultExpireTime.getPurchaseApproveDays()));
            this.setProductionDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getProductionDays, defaultExpireTime.getProductionDays()));
            this.setSupplierDeliveryDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getSupplierDeliveryDays, defaultExpireTime.getSupplierDeliveryDays()));
            this.setQcDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getQcDays, defaultExpireTime.getQcDays()));
            this.setPurchaseCycleDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getPurchaseCycleDays, defaultExpireTime.getPurchaseCycleDays()));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType)) {
                this.setInstockDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getPlatformInstockDays, defaultExpireTime.getPlatformInstockDays()));
            } else {
                this.setInstockDays(getOrDefault(entity, CfgRuleExpireTimeEntity::getOverseasInstockDays, defaultExpireTime.getOverseasInstockDays()));
            }
            this.setRefId(getOrDefault(entity, CfgRuleExpireTimeEntity::getRefId, ""));
            // 设置物流和备货比率结果
            this.setLogisticsResult(logisticsResult);
            this.setLogisticsMinResult(logisticsMinResult);
            this.setLogisticsMaxResult(logisticsMaxResult);
        }

        // 提取默认值的辅助方法
        private <T> T getOrDefault(CfgRuleExpireTimeEntity entity, Function<CfgRuleExpireTimeEntity, T> getter, T defaultValue) {
            return (entity != null && getter.apply(entity) != null) ? getter.apply(entity) : defaultValue;
        }
    }
}
