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
        private String skuId;

        @NotBlank(message = "采购价目详情表id 不能为空")
        private String purchasePriceDetailId;

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
        private String skuId;

        @NotBlank(message = "采购价目详情表id 不能为空")
        private String purchasePriceDetailId;

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
        private BigDecimal taxRate;


        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled;
    }

}
