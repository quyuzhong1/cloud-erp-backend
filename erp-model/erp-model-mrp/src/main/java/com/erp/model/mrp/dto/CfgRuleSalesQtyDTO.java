package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
        * 主键id
        */
        private String id;

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
        @NotNull(message = "断货数据是否从历史销量中排除,true是，false否不能为空")
        private Boolean isIgnoreOutOfStock;

        /**
        * sales_qty_type
        * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
        */
        @NotBlank(message = "sales_qty_type")
        private String salesQtyType;

        /**
        * 订单类型，all:全部，fba:FBA,fbm:FBM
        */
        @NotBlank(message = "订单类型，all:全部，fba:FBA,fbm:FBM不能为空")
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


}