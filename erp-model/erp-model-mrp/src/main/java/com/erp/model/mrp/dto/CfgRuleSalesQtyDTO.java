package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
｜ 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
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


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否同常规品配置一致,true是，false否
        */
        @NotNull(message = "是否同常规品配置一致,true是，false否不能为空")
        private Boolean isCfgSame;

        /**
        * 断货数据是否从历史销量中排除,true是，false否
        */
        @NotNull(message = "断货数据是否从历史销量中排除,true是，false否不能为空")
        private Boolean isIgnoreOutOfStock;

        /**
        * sales_qty_type
｜ 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
        */
        @NotBlank(message = "sales_qty_type
｜ 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量不能为空")
        @Size(max = 32,message = "sales_qty_type
｜ 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量最大长度不能超过32位")
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
        @NotBlank(message = "关联id不能为空")
        @Size(max = 19,message = "关联id最大长度不能超过19位")
        private String refId;

        /**
        * 关联类型
        */
        @NotBlank(message = "关联类型不能为空")
        @Size(max = 32,message = "关联类型最大长度不能超过32位")
        private String refType;

        /**
        * 类型，new 新品、conventional常规品
        */
        @NotBlank(message = "类型，new 新品、conventional常规品不能为空")
        @Size(max = 32,message = "类型，new 新品、conventional常规品最大长度不能超过32位")
        private String type;


    }


}