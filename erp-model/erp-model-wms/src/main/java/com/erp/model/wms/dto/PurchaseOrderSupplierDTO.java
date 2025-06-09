package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 采购订单供应商表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-19
*/
@Data
@NoArgsConstructor
public class PurchaseOrderSupplierDTO implements Serializable {




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
        * 结算币种
        */
        private String payCurrency;

        /**
        * 供应商联系人表id
        */
        private String supplierContactId;

        /**
        * 联系人名称
        */
        private String contactName;

        /**
        * 联系人电话
        */
        private String contactTelNumber;


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
        * 采购订单id
        */
        @NotBlank(message = "采购订单id不能为空")
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String purchaseOrderId;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 200,message = "供应商名称最大长度不能超过200位")
        private String supplierName;

        /**
        * 结算方式
        */
        @NotBlank(message = "结算方式不能为空")
        @Size(max = 19,message = "结算方式最大长度不能超过19位")
        private String payMethodId;

        /**
        * 结算币种
        */
        @NotBlank(message = "结算币种不能为空")
        @Size(max = 10,message = "结算币种最大长度不能超过10位")
        private String payCurrency;

        /**
        * 供应商联系人表id
        */
        @NotBlank(message = "供应商联系人表id不能为空")
        @Size(max = 19,message = "供应商联系人表id最大长度不能超过19位")
        private String supplierContactId;

        /**
        * 联系人名称
        */
        @NotBlank(message = "联系人名称不能为空")
        @Size(max = 30,message = "联系人名称最大长度不能超过30位")
        private String contactName;

        /**
        * 联系人电话
        */
        @NotBlank(message = "联系人电话不能为空")
        @Size(max = 15,message = "联系人电话最大长度不能超过15位")
        private String contactTelNumber;


    }


}