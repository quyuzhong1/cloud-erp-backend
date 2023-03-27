package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
        private Integer deliveryDate;

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


    }
    @Data
    @NoArgsConstructor
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
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO  extends AddDTO{

        @NotBlank(message = "id 不能为空")
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
    }

}
