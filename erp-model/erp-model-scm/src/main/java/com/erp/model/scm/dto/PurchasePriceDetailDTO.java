package com.erp.model.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailDTO
 * @Description TODO
 * @Date 2023-03-16 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;


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
         * 生效时间
         */
        private LocalDate effectiveDate;


        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;


        /**
         * true 禁用
         * false 启用
         * 默认false
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseTaxPriceSearchDTO {

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        private Integer purchaseQty;

        /**
         * sku id
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 供应商id
         */
        private String supplierId;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class PurchaseTaxPriceViewDTO {

        /**
         * 供应商表id
         */
        private String supplierId;

        /**
         * 供应商表名称
         */
        private String supplierName;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 币别
         */
        private String currency;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {


        /**
         * 成功返回数据
         */
        private List<PurchasePriceDetailDTO.AddDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO{


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
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        private LocalDate expireDate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币别符号
         */
        private String currencySymbol;


    }



}
