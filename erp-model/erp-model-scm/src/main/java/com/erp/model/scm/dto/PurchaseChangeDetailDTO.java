package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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
        @NotEmpty(message = "采购数量不能为空")
        private Integer qty;

        /**
         * 新含税单价
         */
        @NotEmpty(message = "新含税单价不能为空")
        private BigDecimal price;

        /**
         * 新含税金额
         */
        private BigDecimal amount;

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
