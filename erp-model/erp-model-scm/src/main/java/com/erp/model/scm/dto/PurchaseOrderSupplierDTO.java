package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 14:38
 */
@Data
@NoArgsConstructor
public class PurchaseOrderSupplierDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 结算币种
         */
        private String payCurrency;

        /**
         * 供应商联系人表id
         */
        private String supplierContactId;

        /**
         * 联系人电话
         */
        private String contactTelNumber;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }


    @Data
    @NoArgsConstructor
    public static class SupplierPurchaseDTO {

        /**
         * 采购单号
         */
        private String code;


        /**
         * 采购数量
         */
        private String purchaseQty;


        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;


        /**
         * 采购状态
         */
        private String approveStatus;

        /**
         * 采购状态名
         */
        private String approveStatusName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

}
