package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 15:49
 */
@Data
@NoArgsConstructor
public class PurchaseChangeDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 采购变更单id
         */
        private String purchaseChangeId;

        /**
         * 采购订单明细id
         */
        @NotBlank(message = "采购订单明细id不能为空")
        private String purchaseOrderDetailId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 原采购数量
         */
        private Integer oldQty;

        /**
         * 原含税单价
         */
        private BigDecimal oldPrice;

        /**
         * 原含税金额
         */
        private BigDecimal oldAmount;

        /**
         * 新采购数量
         */
        @NotNull(message = "新采购数量不能为空")
        @Min(value = 0,message = "新采购数量最小值为0")
        @Max(value = 999999999,message = "新采购数量最大值为999999999")
        private Integer qty;

        /**
         * 新含税单价
         */
        @NotNull(message = "新含税单价不能为空")
        @Digits(integer = 16,fraction = 4,message = "新含税单价最大16字符，小数位不能大于4个字符")
        private BigDecimal price;

        /**
         * 新含税金额
         */
        private BigDecimal amount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 变更备注
         */
        @Size(max = 255,message = "变更备注不能大于255字符")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        private String id;
    }

}
