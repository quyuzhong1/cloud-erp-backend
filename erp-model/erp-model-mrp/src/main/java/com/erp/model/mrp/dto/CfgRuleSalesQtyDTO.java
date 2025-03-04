package com.erp.model.mrp.dto;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销量（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleSalesQtyDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 断货数据是否从历史销量中排除,true是，false否
         */
        private Boolean isIgnoreOutOfStock;

        /**
         * sales_qty_type
         * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
         */
        private String salesQtyType;

        /**
         * 订单类型，订单类型，亚马逊取FbaOrderTypeEnum，海外取OverseasOrderTypeEnum
         */
        private List<String> orderType;

        /**
         * 平台类型
         */
        private String platform;

        /**
         * 预估销量类型（SYSTEM/AI/CUSTOMER）
         */
        private String salesEstimateType;

        /**
         * 是否同常规品设置
         */
        private Boolean isCfgSameDefault;
        /**
         * 默认日销量
         */
        private CfgRuleSalesFormulaDTO.ViewDTO defaultSalesQtyDTO;

        /**
         * 新品默认日销量
         */
        private CfgRuleSalesFormulaDTO.ViewDTO defaultNewSalesQtyDTO;

        /**
         * 是否同常规品设置
         */
        private Boolean isCfgSameDynamic;
        /**
         * 动态日销量
         */
        private List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicSalesQtyList;
        /**
         * 新品动态日销量
         */
        private List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicNewSalesQtyList;
        /**
         * 固定日销量
         */
        private List<CfgRuleSalesFormulaDTO.ViewDTO> fixedSalesQtyList;

        /**
         * 是否同常规品设置
         */
        private Boolean isCfgSameDenoising;
        /**
         *销量去噪
         */
        private List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingList;
        /**
         *新品销量去噪
         */
        private List<CfgRuleSalesDenoisingDTO.ViewDTO> newSalesDenoisingList;
    }


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDetailDTO {

        /**
        * 主键id
        */
        private String  id;

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
         * 默认日销量
         */
        private CfgRuleSalesFormulaDTO.ViewDTO defaultSalesQtyDTO;

        /**
         * 动态日销量
         */
        private List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicSalesQtyList;

        /**
         * 固定日销量
         */
        private List<CfgRuleSalesFormulaDTO.ViewDTO> fixedSalesQtyList;

        /**
         *销量去噪
         */
        private List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 断货数据是否从历史销量中排除,true是，false否
         */
        @NotNull(message = "断货排除不能为空")
        private Boolean isIgnoreOutOfStock;

        /**
         * sales_qty_type
         * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
         */
        private String salesQtyType;

        /**
         * 订单类型，订单类型，亚马逊取FbaOrderTypeEnum，海外取OverseasOrderTypeEnum
         */
        private List<String> orderType;

        /**
         * 预估销量类型（SYSTEM/AI/CUSTOMER）
         */
        @NotNull(message = "预估销量类型不能为空")
        private String salesEstimateType;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 32,message = "平台类型最大长度不能超过32位")
        private String platform;

        /**
         * 关联id
         */
        @Size(max = 19,message = "关联id最大长度不能超过19位")
        private String refId;

        /**
         * 关联类型
         */
        @Size(max = 32,message = "关联类型最大长度不能超过32位")
        private String refType;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 是否同常规品设置
         */
        @NotNull(message = "是否同常规品设置不能为空")
        private Boolean isCfgSameDefault;
        /**
         * 默认日销量
         */
        @Valid
        @NotNull(message = "常规品默认设置不能为空")
        private CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultSalesQtyDTO;

        /**
         * 新品默认日销量
         */
        @Valid
        @NotNull(message = "新品默认设置不能为空")
        private CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultNewSalesQtyDTO;

        /**
         * 是否同常规品设置
         */
        @NotNull(message = "是否同常规品设置不能为空")
        private Boolean isCfgSameDynamic;
        /**
         * 动态日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.DynamicUpdateDTO> dynamicSalesQtyList;
        /**
         * 新品动态日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.DynamicUpdateDTO> dynamicNewSalesQtyList;
        /**
         * 固定日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.FixedUpdateDTO> fixedSalesQtyList;

        /**
         * 是否同常规品设置
         */
        @NotNull(message = "是否同常规品设置不能为空")
        private Boolean isCfgSameDenoising;
        /**
         *销量去噪
         */
        @Valid
        private List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList;
        /**
         *新品销量去噪
         */
        @Valid
        private List<CfgRuleSalesDenoisingDTO.UpdateDTO> newSalesDenoisingList;
    }


    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class CommonDTO {

        /**
        * 是否同常规品配置一致,true是，false否
        */
        private Boolean isCfgSame;

        /**
        * 断货数据是否从历史销量中排除,true是，false否
        */
        private Boolean isIgnoreOutOfStock;

        /**
        * sales_qty_type
        * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
        */
        private String salesQtyType;

        /**
        * 订单类型，订单类型，亚马逊取FbaOrderTypeEnum，海外取OverseasOrderTypeEnum
        */
        private List<String> orderType;

        /**
        * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
        */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32,message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;

        /**
        * 关联id
        */
        @Size(max = 19,message = "关联id最大长度不能超过19位")
        private String refId;

        /**
        * 关联类型
        */
        @Size(max = 32,message = "关联类型最大长度不能超过32位")
        private String refType;

        /**
        * 类型，new 新品、conventional常规品
        */
        @Size(max = 32,message = "类型，new 新品、conventional常规品最大长度不能超过32位")
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategyDTO {
        /**
         * 建议id
         */
        private String refId;
        /**
         * 平台类型
         */
        private String platform;
        /**
         * sku类型
         */
        private String skuType;
        /**
         * 销量配置
         */
        private List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList;
        /**
         * 去噪配置
         */
        private List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList;
        /**
         * 销量配置
         */
        private List<CfgRuleSalesFormulaEntity> cfgRuleSalesFormulaList;

        public static StrategyDTO buildStrategyDTO(ReplenishmentResultDTO.BasicDTO entity, String skuType, List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList,
                                                   List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList,
                                                   List<CfgRuleSalesFormulaEntity> cfgRuleSalesFormulaList) {
            StrategyDTO resultDTO = new StrategyDTO();
            resultDTO.setRefId(entity.getId());
            resultDTO.setPlatform(entity.getPlatform());
            resultDTO.setSkuType(skuType);
            resultDTO.setCfgRuleSalesQtyList(cfgRuleSalesQtyList);
            resultDTO.setCfgRuleSalesDenoisingList(cfgRuleSalesDenoisingList);
            resultDTO.setCfgRuleSalesFormulaList(cfgRuleSalesFormulaList);
            return resultDTO;
        }
    }

    @Getter
    @Setter
    public static class StrategyResultDTO {
        /**
         * 断货数据是否从历史销量中排除,true是，false否
         */
        private Boolean isIgnoreOutOfStock;
        /**
         * sales_qty_type
         * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
         */
        private String salesQtyType;
        /**
         * 订单类型，all:全部，fba:FBA,fbm:FBM
         */
        private JSONArray orderType;
        /**
         * 平台类型
         */
        private String platform;
        /**
         * 预估销量类型（SYSTEM/AI/CUSTOMER）
         */
        private String salesEstimateType;
        /**
         * 关联id
         */
        private String refId;
        /**
         * 降噪配置
         */
        private List<StrategyDenoisingResultDTO> denoisingResults;
        /**
         * 销量计算配置
         */
        private List<StrategyFormulaResultDTO> formulaResults;

        /**
         * 默认降噪配置
         */
        private List<StrategyDenoisingResultDTO> defaultDenoisingResults;
        /**
         * 默认销量计算配置
         */
        private List<StrategyFormulaResultDTO> defaultFormulaResults;

        public static StrategyResultDTO buildStrategyResultDTO(CfgRuleSalesQtyEntity cfgRuleSalesQty, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults,
                                                               List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults,
                                                               List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults,
                                                               List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoisingResults) {
            StrategyResultDTO resultDTO = new StrategyResultDTO();
            resultDTO.setIsIgnoreOutOfStock(cfgRuleSalesQty.getIsIgnoreOutOfStock());
            resultDTO.setSalesQtyType(cfgRuleSalesQty.getSalesQtyType());
            resultDTO.setOrderType(cfgRuleSalesQty.getOrderType());
            resultDTO.setPlatform(cfgRuleSalesQty.getPlatform());
            resultDTO.setSalesEstimateType(cfgRuleSalesQty.getSalesEstimateType());
            resultDTO.setRefId(cfgRuleSalesQty.getRefId());
            resultDTO.setFormulaResults(formulaResults);
            resultDTO.setDenoisingResults(denoisingResults);
            resultDTO.setDefaultFormulaResults(defaultFormulaResults);
            resultDTO.setDefaultDenoisingResults(defaultDenoisingResults);
            return resultDTO;
        }
    }

    @Getter
    @Setter
    public static class StrategyDenoisingResultDTO {
        /**
         * id
         */
        private String id;
        /**
         * 序号
         */
        private Integer index;
        /**
         * 名称
         */
        private String name;
        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
        /**
         * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪  枚举：CfgRuleSalesDenoisingDenoisingTypeEnum
         */
        private String denoisingType;
        /**
         * 有效值（去噪后的）
         */
        private Integer effectiveValue;

        public static CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO buildStrategyDenoisingResultDTO(CfgRuleSalesDenoisingEntity entity) {
            StrategyDenoisingResultDTO dto = new StrategyDenoisingResultDTO();
            dto.setId(entity.getId());
            dto.setIndex(entity.getIndex());
            dto.setName(entity.getName());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setDenoisingType(entity.getDenoisingType());
            dto.setEffectiveValue(entity.getEffectiveValue());
            return dto;
        }

        public static StrategyDenoisingResultDTO buildStrategyDenoisingResultDTO(CfgRuleSalesDenoisingDTO.UpdateDTO updateDTO) {
            StrategyDenoisingResultDTO dto = new StrategyDenoisingResultDTO();
            dto.setName(updateDTO.getName());
            dto.setStartDate(updateDTO.getDateList().get(0));
            dto.setEndDate(updateDTO.getDateList().get(1));
            dto.setDenoisingType(updateDTO.getDenoisingType());
            dto.setEffectiveValue(updateDTO.getEffectiveValue());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class StrategyFormulaResultDTO {
        /**
         * id
         */
        private String id;
        /**
         * 销量类型：default=默认，dynamic=动态、fixed=固定  枚举：CfgRuleSalesFormulaTypeEnum
         */
        private String type;
        /**
         * 销量默认类型：dynamic=动态、fixed=固定  枚举：CfgRuleSalesFormulaDefaultTypeEnum
         */
        private String defaultType;
        /**
         * 排序字段
         */
        private Integer index;
        /**
         * 优先级字段
         */
        private Integer priority;
        /**
         * 名称
         */
        private String name;
        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
        /**
         * 销量id(cfg_rule_sales_qty)
         */
        private String salesQtyId;
        /**
         * 固定值
         */
        private Integer fixedValue;
        /**
         * 百分比json
         */
        private JSONObject percentJson;
        /**
         * 百分比对象
         */
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;

        public static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO buildFormulaResultDTO(CfgRuleSalesFormulaEntity entity) {
            StrategyFormulaResultDTO dto = new StrategyFormulaResultDTO();
            dto.setId(entity.getId());
            dto.setType(entity.getType());
            dto.setDefaultType(entity.getDefaultType());
            dto.setIndex(entity.getIndex());
            dto.setPriority(entity.getPriority());
            dto.setName(entity.getName());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setSalesQtyId(entity.getSalesQtyId());
            dto.setFixedValue(entity.getFixedValue());
            dto.setPercentJson(entity.getPercentJson());
            dto.setPercentJsonDTO(JSONUtil.toBean(entity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class));
            return dto;
        }

        public static StrategyFormulaResultDTO buildFormulaResultDTO(CfgRuleSalesFormulaDTO.FixedUpdateDTO fixedUpdate) {
            StrategyFormulaResultDTO dto = new StrategyFormulaResultDTO();
            dto.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode());
            dto.setName(fixedUpdate.getName());
            dto.setFixedValue(fixedUpdate.getFixedValue());
            return dto;
        }

        public static StrategyFormulaResultDTO buildFormulaResultDTO(CfgRuleSalesFormulaDTO.DynamicUpdateDTO dynamicUpdate) {
            StrategyFormulaResultDTO dto = new StrategyFormulaResultDTO();
            dto.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode());
            dto.setName(dynamicUpdate.getName());
            dto.setPercentJsonDTO(dynamicUpdate.getPercentJsonDTO());
            return dto;
        }

        public static StrategyFormulaResultDTO buildFormulaResultDTO(CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultUpdateDTO) {
            StrategyFormulaResultDTO dto = new StrategyFormulaResultDTO();
            dto.setDefaultType(defaultUpdateDTO.getDefaultType());
            dto.setPercentJsonDTO(defaultUpdateDTO.getPercentJsonDTO());
            dto.setFixedValue(defaultUpdateDTO.getFixedValue());
            return dto;
        }
    }

}