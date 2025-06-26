package com.erp.model.dmp.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * SKU销量报告请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-06-23
*/
@Data
@NoArgsConstructor
public class DwsDbErpDmpSkuSalesReportFDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 日均销量类型：dailyAvg3Days、dailyAvg7Days、dailyAvg30Days、dailyAvg60Days、dailyAvg90Days
        */
        private String dailySalesType;

        /**
        * 日均销量
        */
        private Integer dailySales;

        /**
        * 统计维度：deliveryTime=出库时间，paymentTime=付款时间
        */
        private String dimension;

        /**
        * SKU唯一ID
        */
        private String skuId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * SKU名称
        */
        private String skuName;

        /**
        * 销售状态编码
        */
        private String saleState;

        /**
        * 销售状态名称
        */
        private String saleStateName;

        /**
        * SKU图片URL
        */
        private String productImageUrl;

        /**
        * 一级分类ID
        */
        private String firstCategoryId;

        /**
        * 一级分类名称
        */
        private String firstCategoryName;

        /**
        * 二级分类ID
        */
        private String secondCategoryId;

        /**
        * 二级分类名称
        */
        private String secondCategoryName;

        /**
        * 销售平台编码
        */
        private String dictPlatform;

        /**
        * 销售平台名称
        */
        private String platformName;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家ID
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 军区ID
        */
        private String partitionId;

        /**
        * 军区名称
        */
        private String partitionName;

        /**
        * 近3日销量
        */
        private Integer salesLast3Days;

        /**
        * 近7日销量
        */
        private Integer salesLast7Days;

        /**
        * 近30日销量
        */
        private Integer salesLast30Days;

        /**
        * 近60日销量
        */
        private Integer salesLast60Days;

        /**
        * 近90日销量
        */
        private Integer salesLast90Days;

        /**
        * 统计日期
        */
        private LocalDate statDate;


    }



    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 日均销量类型：dailyAvg3Days、dailyAvg7Days、dailyAvg30Days、dailyAvg60Days、dailyAvg90Days
        */
        @NotBlank(message = "日均销量类型：dailyAvg3Days、dailyAvg7Days、dailyAvg30Days、dailyAvg60Days、dailyAvg90Days不能为空")
        @Size(max = 64,message = "日均销量类型：dailyAvg3Days、dailyAvg7Days、dailyAvg30Days、dailyAvg60Days、dailyAvg90Days最大长度不能超过64位")
        private String dailySalesType;

        /**
        * 日均销量
        */
        @NotNull(message = "日均销量不能为空")
        private Integer dailySales;

        /**
        * 统计维度：deliveryTime=出库时间，paymentTime=付款时间
        */
        @NotBlank(message = "统计维度：deliveryTime=出库时间，paymentTime=付款时间不能为空")
        @Size(max = 64,message = "统计维度：deliveryTime=出库时间，paymentTime=付款时间最大长度不能超过64位")
        private String dimension;

        /**
        * SKU唯一ID
        */
        @NotBlank(message = "SKU唯一ID不能为空")
        @Size(max = 64,message = "SKU唯一ID最大长度不能超过64位")
        private String skuId;

        /**
        * SKU名称
        */
        @NotBlank(message = "SKU名称不能为空")
        private String skuName;

        /**
        * 销售状态编码
        */
        @NotBlank(message = "销售状态编码不能为空")
        @Size(max = 32,message = "销售状态编码最大长度不能超过32位")
        private String saleState;

        /**
        * 销售状态名称
        */
        @NotBlank(message = "销售状态名称不能为空")
        @Size(max = 64,message = "销售状态名称最大长度不能超过64位")
        private String saleStateName;

        /**
        * SKU图片URL
        */
        @NotBlank(message = "SKU图片URL不能为空")
        private String productImageUrl;

        /**
        * 一级分类ID
        */
        @NotBlank(message = "一级分类ID不能为空")
        @Size(max = 64,message = "一级分类ID最大长度不能超过64位")
        private String firstCategoryId;

        /**
        * 一级分类名称
        */
        @NotBlank(message = "一级分类名称不能为空")
        @Size(max = 100,message = "一级分类名称最大长度不能超过100位")
        private String firstCategoryName;

        /**
        * 二级分类ID
        */
        @NotBlank(message = "二级分类ID不能为空")
        @Size(max = 64,message = "二级分类ID最大长度不能超过64位")
        private String secondCategoryId;

        /**
        * 二级分类名称
        */
        @NotBlank(message = "二级分类名称不能为空")
        @Size(max = 100,message = "二级分类名称最大长度不能超过100位")
        private String secondCategoryName;

        /**
        * 销售平台编码
        */
        @NotBlank(message = "销售平台编码不能为空")
        @Size(max = 64,message = "销售平台编码最大长度不能超过64位")
        private String dictPlatform;

        /**
        * 销售平台名称
        */
        @NotBlank(message = "销售平台名称不能为空")
        @Size(max = 100,message = "销售平台名称最大长度不能超过100位")
        private String platformName;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 64,message = "店铺ID最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 100,message = "店铺名称最大长度不能超过100位")
        private String shopName;

        /**
        * 国家ID
        */
        @NotBlank(message = "国家ID不能为空")
        @Size(max = 64,message = "国家ID最大长度不能超过64位")
        private String countryId;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 100,message = "国家名称最大长度不能超过100位")
        private String countryName;

        /**
        * 军区ID
        */
        @NotBlank(message = "军区ID不能为空")
        @Size(max = 64,message = "军区ID最大长度不能超过64位")
        private String partitionId;

        /**
        * 军区名称
        */
        @NotBlank(message = "军区名称不能为空")
        @Size(max = 100,message = "军区名称最大长度不能超过100位")
        private String partitionName;

        /**
        * 近3日销量
        */
        @NotNull(message = "近3日销量不能为空")
        private Integer salesLast3Days;

        /**
        * 近7日销量
        */
        @NotNull(message = "近7日销量不能为空")
        private Integer salesLast7Days;

        /**
        * 近30日销量
        */
        @NotNull(message = "近30日销量不能为空")
        private Integer salesLast30Days;

        /**
        * 近60日销量
        */
        @NotNull(message = "近60日销量不能为空")
        private Integer salesLast60Days;

        /**
        * 近90日销量
        */
        @NotNull(message = "近90日销量不能为空")
        private Integer salesLast90Days;

        /**
        * 统计日期
        */
        private LocalDate statDate;


    }

    @Data
    @NoArgsConstructor
    public static class RequestListDTO implements Serializable {

        /**
         * 子字查询sql
         */
        private String conditionSql;
    }
}