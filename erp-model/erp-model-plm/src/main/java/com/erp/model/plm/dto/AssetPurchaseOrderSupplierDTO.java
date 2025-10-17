package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@NoArgsConstructor
public class AssetPurchaseOrderSupplierDTO implements Serializable {




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
        * 资产采购单id
        */
        private String assetPurchaseOrderId;

        /**
        * 供应商id
        */
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
        * 供应商联系人id
        */
        private String contactId;

        /**
        * 供应商联系人名称
        */
        private String contactName;

        /**
        * 供应商电话
        */
        private String contactTelNumber;

        /**
        * 付款条件
        */
        private String paymentCondition;

        /**
        * 付款条件名称
        */
        private String paymentConditionName;

        /**
        * 账户名称
        */
        private String payee;

        /**
        * 收款银行
        */
        private String bankName;

        /**
        * 银行账号
        */
        private String bankAccount;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 资产采购单id
        */
        @NotBlank(message = "资产采购单id不能为空")
        private String assetPurchaseOrderId;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;

        /**
        * 结算方式
        */
        @NotBlank(message = "结算方式不能为空")
        private String payMethodId;

        /**
        * 结算币种
        */
        private String payCurrency;

        /**
        * 供应商联系人id
        */
        private String contactId;

        /**
        * 供应商联系人名称
        */
        private String contactName;

        /**
        * 供应商电话
        */
        private String contactTelNumber;

        /**
        * 付款条件
        */
        @NotBlank(message = "付款条件不能为空")
        private String paymentCondition;

        /**
        * 付款条件名称
        */
        @NotBlank(message = "付款条件名称不能为空")
        private String paymentConditionName;

        /**
        * 账户名称
        */
        @NotBlank(message = "账户名称不能为空")
        private String payee;

        /**
        * 收款银行
        */
        @NotBlank(message = "收款银行不能为空")
        private String bankName;

        /**
        * 银行账号
        */
        @NotBlank(message = "银行账号不能为空")
        private String bankAccount;


    }


}