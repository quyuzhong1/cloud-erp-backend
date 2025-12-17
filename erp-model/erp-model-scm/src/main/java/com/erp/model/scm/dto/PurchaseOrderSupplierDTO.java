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
        @NotBlank(message = "结算方式不能为空")
        private String payMethodId;
        /**
         * 结算方式
         */
        private String payMethodName;

        /**
         * 结算币种
         */
        private String payCurrency;
        /**
         * 结算币种名称
         */
        private String payCurrencyName;

        /**
         * 供应商联系人表id
         */
        private String supplierContactId;

        /**
         * 联系人电话
         */
        private String contactTelNumber;

        /**
         * 付款条件
         */
        @NotBlank(message = "付款条件不能为空")
        private String paymentCondition;

        /**
         * 付款条件
         */
        private String paymentConditionName;

        /**
         * 供应商账户id
         * http://172.16.100.11:3002/project/83/interface/api/36188
         */
        private String supplierAccountId;
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

    @Data
    @NoArgsConstructor
    public static class PdaView {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 结算方式
         */
        private String payMethodName;

        /**
         * 结算币种
         */
        private String payCurrency;

        /**
         * 供应商联系人表id
         */
        private String supplierContactId;

        /**
         * 供应商联系人名称
         */
        private String supplierContactName;

        /**
         * 供应商联系地址
         */
        private String supplierAddress;

        /**
         * 联系人电话
         */
        private String contactTelNumber;

        /**
         * 付款条件
         */
        private String paymentCondition;
    }
}
