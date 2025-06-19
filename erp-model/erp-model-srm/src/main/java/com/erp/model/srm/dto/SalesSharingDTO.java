package com.erp.model.srm.dto;

import java.math.BigDecimal;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 销量共享表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-18
*/
@Data
@NoArgsConstructor
public class SalesSharingDTO implements Serializable {




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
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 产品图片
        */
        private String productImage;

        /**
        * 产品图片url
        */
        private String productImageUrl;

        /**
        * sku_id
        */
        private String skuId;

        /**
        * SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String skuName;

        /**
        * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
        */
        private Integer saleState;

        /**
        * 可销库存
        */
        private BigDecimal saleableStock;

        /**
        * 原始日均销量
        */
        private BigDecimal dailySales;

        /**
        * 近3日销量
        */
        private BigDecimal salesLast3Days;

        /**
        * 近7日销量
        */
        private BigDecimal salesLast7Days;

        /**
        * 近30日销量
        */
        private BigDecimal salesLast30Days;

        /**
        * 近60日销量
        */
        private BigDecimal salesLast60Days;

        /**
        * 近90日销量
        */
        private BigDecimal salesLast90Days;

        /**
        * 原始销量比例
        */
        private BigDecimal salesRatio;

        /**
        * 可销天数
        */
        private Integer saleableDays;


    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 产品图片
         */
        private String productImage;

        /**
         * 产品图片url
         */
        private String productImageUrl;

        /**
         * sku_id
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String skuName;

        /**
         * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
         */
        private Integer saleState;
        private String saleStateName;

        /**
         * 可销库存
         */
        private BigDecimal saleableStock;

        /**
         * 原始日均销量
         */
        private BigDecimal dailySales;

        /**
         * 近3日销量
         */
        private BigDecimal salesLast3Days;

        /**
         * 近7日销量
         */
        private BigDecimal salesLast7Days;

        /**
         * 近30日销量
         */
        private BigDecimal salesLast30Days;

        /**
         * 近60日销量
         */
        private BigDecimal salesLast60Days;

        /**
         * 近90日销量
         */
        private BigDecimal salesLast90Days;

        /**
         * 原始销量比例
         */
        private BigDecimal salesRatio;

        /**
         * 可销天数
         */
        private Integer saleableDays;
        /**
         * 是否需要进行通知
         */
        private Boolean needNotice;
        /**
         * 通知内容
         */
        private String noticeContent;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 64,message = "供应商id最大长度不能超过64位")
        private String supplierId;

        /**
        * 供应商编码
        */
        @NotBlank(message = "供应商编码不能为空")
        @Size(max = 32,message = "供应商编码最大长度不能超过32位")
        private String supplierCode;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 500,message = "供应商名称最大长度不能超过500位")
        private String supplierName;

        /**
        * 产品图片
        */
        @NotBlank(message = "产品图片不能为空")
        @Size(max = 64,message = "产品图片最大长度不能超过64位")
        private String productImage;

        /**
        * 产品图片url
        */
        @NotBlank(message = "产品图片url不能为空")
        @Size(max = 255,message = "产品图片url最大长度不能超过255位")
        private String productImageUrl;

        /**
        * sku_id
        */
        @NotBlank(message = "sku_id不能为空")
        @Size(max = 19,message = "sku_id最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String skuName;

        /**
        * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
        */
        private Integer saleState;

        /**
        * 可销库存
        */
        @NotNull(message = "可销库存不能为空")
        @Digits(integer = 12, fraction = 4, message = "可销库存整数位不能超过12位，小数位不能超过4位")
        private BigDecimal saleableStock;

        /**
        * 原始日均销量
        */
        @NotNull(message = "原始日均销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "原始日均销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal dailySales;

        /**
        * 近3日销量
        */
        @NotNull(message = "近3日销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "近3日销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesLast3Days;

        /**
        * 近7日销量
        */
        @NotNull(message = "近7日销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "近7日销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesLast7Days;

        /**
        * 近30日销量
        */
        @NotNull(message = "近30日销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "近30日销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesLast30Days;

        /**
        * 近60日销量
        */
        @NotNull(message = "近60日销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "近60日销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesLast60Days;

        /**
        * 近90日销量
        */
        @NotNull(message = "近90日销量不能为空")
        @Digits(integer = 12, fraction = 4, message = "近90日销量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesLast90Days;

        /**
        * 原始销量比例
        */
        @NotNull(message = "原始销量比例不能为空")
        @Digits(integer = 12, fraction = 4, message = "原始销量比例整数位不能超过12位，小数位不能超过4位")
        private BigDecimal salesRatio;

        /**
        * 可销天数
        */
        @NotNull(message = "可销天数不能为空")
        private Integer saleableDays;


    }


}