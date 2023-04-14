package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailDTO
 * @Description TODO
 * @Date 2023-03-16 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceChangeDetailDTO implements Serializable {


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "采购价目详情表id 不能为空")
        private String purchasePriceDetailId;

        /**
         * 采购交期
         */
        private Integer deliveryDay;
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
        private BigDecimal taxPrice;

        /**
         * 生效时间
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

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

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {


        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "采购价目详情表id 不能为空")
        private String purchasePriceDetailId;

        /**
         * 采购交期
         */
        private Integer deliveryDay;
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
        private BigDecimal taxPrice;

        /**
         * 生效时间
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

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
    }



    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;

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


        private String purchasePriceDetailId;

        /**
         * 采购交期
         */
        private Integer deliveryDay;
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
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 调后税率
         */
        private BigDecimal taxRate;

        /**
         * 调前税率
         */
        private BigDecimal oldTaxRate;




        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled=false;
    }

}
