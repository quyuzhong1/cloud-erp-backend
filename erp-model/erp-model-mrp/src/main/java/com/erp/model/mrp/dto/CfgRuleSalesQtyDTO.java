package com.erp.model.mrp.dto;

import cn.hutool.json.JSONObject;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
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
         * 是否同常规品设置
         */
        private Boolean isCfgSame;

        /**
         * 常规品
         */
        private ViewDetailDTO conventionalDetail;

        /**
         * 新品
         */
        private ViewDetailDTO newDetail;
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
        * 订单类型，all:全部，fba:FBA,fbm:FBM
        */
        private String orderType;

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
        * 类型，new 新品、conventional常规品
        */
        private String type;

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
         * 是否同常规品设置
         */
        @NotNull(message = "是否同常规品设置不能为空")
        private Boolean isCfgSame;

        /**
         * 常规品
         */
        @Valid
        @NotNull(message = "常规品设置不能为空")
        private UpdateDetailDTO conventionalDetail;

        /**
         * 新品
         */
        @Valid
        @NotNull(message = "新品设置不能为空")
        private UpdateDetailDTO newDetail;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class UpdateDetailDTO extends CommonDTO {
        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 默认日销量
         */
        @NotNull(message = "默认日销量不能为空")
        @Valid
        private CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultSalesQtyDTO;

        /**
         * 动态日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.DynamicUpdateDTO> dynamicSalesQtyList;

        /**
         * 固定日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.FixedUpdateDTO> fixedSalesQtyList;

        /**
         *销量去噪
         */
        @Valid
        private List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList;

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
        * 订单类型，all:全部，fba:FBA,fbm:FBM
        */
        @Size(max = 32,message = "订单类型，all:全部，fba:FBA,fbm:FBM最大长度不能超过32位")
        private String orderType;

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
    public static class StrategyDTO {
        /**
         * 建议id
         */
        private String refId;
        /**
         * sku类型
         */
        private String skuType;
        /**
         * 平台类型
         */
        private String platformType;
    }

    @Getter
    @Setter
    public static class StrategyResultDTO {
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
         * 订单类型，all:全部，fba:FBA,fbm:FBM
         */
        private String orderType;
        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;
        /**
         * 关联id
         */
        private String refId;
        /**
         * 类型，new 新品、conventional常规品
         */
        private String type;
        /**
         * 降噪配置
         */
        private List<StrategyDenoisingResultDTO> denoisingResults;
        /**
         * 销量计算配置
         */
        private List<StrategyFormulaResultDTO> formulaResults;

        public static StrategyResultDTO buildStrategyResultDTO(CfgRuleSalesQtyEntity cfgRuleSalesQty, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults, List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults) {
            StrategyResultDTO resultDTO = new StrategyResultDTO();
            resultDTO.setIsCfgSame(cfgRuleSalesQty.getIsCfgSame());
            resultDTO.setIsIgnoreOutOfStock(cfgRuleSalesQty.getIsIgnoreOutOfStock());
            resultDTO.setSalesQtyType(cfgRuleSalesQty.getSalesQtyType());
            resultDTO.setOrderType(cfgRuleSalesQty.getOrderType());
            resultDTO.setPlatformType(cfgRuleSalesQty.getPlatformType());
            resultDTO.setRefId(cfgRuleSalesQty.getRefId());
            resultDTO.setType(cfgRuleSalesQty.getType());
            resultDTO.setFormulaResults(formulaResults);
            resultDTO.setDenoisingResults(denoisingResults);
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
        private BigDecimal fixedValue;
        /**
         * 百分比json
         */
        private JSONObject percentJson;
        /**
         * 百分比对象
         */
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;
    }

}