package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 产品销售变更价 明细表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoPriceChangeDetailDTO implements Serializable {



    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable{

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 客户id
         */
        @NotBlank(message = "客户id能为空")
        private String customerId;

        /**
         * 价目表编码
         */
        private String priceCode;

        /**
         * 销售价目详情表id
         */
        @NotBlank(message = "销售价目详情表id 不能为空")
        private String soPriceDetailId;

        /**
         * 最小数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间从 不能为空")
        private Integer minQty;


        /**
         * 最大数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer maxQty;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @DecimalMin(value = "0",message ="含税单价必须大于0",inclusive = false )
        private BigDecimal taxPrice;

        /**
         * 生效时间
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        @NotNull(message = "失效日期不能为空")
        private LocalDate expireDate;

        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO implements Serializable{


        @NotBlank(message = "id不能为空")
        private String id;


        /**
         * 客户表id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;

        /**
         * 价目表编码
         */
        private String priceCode;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "销售价目详情表id 不能为空")
        private String soPriceDetailId;

        /**
         * 最小数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer minQty;


        /**
         * 最大数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer maxQty;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @DecimalMin(value = "0",message ="含税单价必须大于0",inclusive = false )
        private BigDecimal taxPrice;

        /**
         * 生效时间
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        @NotNull(message = "失效日期不能为空")
        private LocalDate expireDate;

        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;


        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;
    }



    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;

        /**
         * 价目表Code
         */
        private String priceCode;

        /**
         * 客户表id
         */
        private String customerId;


        /**
         * 客户名称
         */
        private String customerName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku_no
         */
        private String skuNo;

        /**
         * 价目表详情id
         */
        private String soPriceDetailId;

        /**
         * 最小数量
         */
        private Integer minQty;


        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币种
         */
        private String currency;


        /**
         * 币别符号
         */
        private String currencySymbol;


        /**
         * 调前币种
         */
        private String oldCurrency;

        /**
         * 调后含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 调前含税单价
         */
        private BigDecimal oldTaxPrice;

        /**
         * 原生效时间
         */
        private LocalDate oldEffectiveDate;

        /**
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 调后税率
         */
        private BigDecimal taxRate;

        /**
         * 调前税率
         */
        private BigDecimal oldTaxRate;


        /**
         * 备注
         */
        private String remark;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled=false;

        /**
         * 销售组织名称
         */
        private String soOrgName;

        /**
         * 升降比例（带百分比）
         */
        private String offsetRate;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class SkuChangeParamDTO {
        /**
         * id
         */
        private List<String> soPriceIds;
        /**
         * 价目表编号
         */
        private String priceCode;
    }

}