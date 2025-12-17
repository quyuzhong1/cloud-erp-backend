package com.erp.model.oms.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname InvoiceDTO

 * @Date 2023-05-10 17:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class InvoiceDTO implements Serializable {

    /**
     * 增加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 发票抬头
         */
        @NotBlank(message = "发票抬头不能为空")
        @Size(max = 50, message = "发票抬头最大50字符")
        private String head;

        /**
         * 发票类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         *          type=InvoiceType
         *
         */
        @StateEnumValue(strValues = {"valueAddedTax","invoice"},message = "发票类型有误")
        private String type;

        /**
         * 开户银行
         */
        @NotBlank(message = "开户银行不能为空")
        @Size(max = 50, message = "开户银行最大50字符")
        private String bankName;

        /**
         * 银行账号
         */
        @NotBlank(message = "银行账号不能为空")
        @Size(max = 50, message = "银行账号最大50字符")
        private String bankAccount;



        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;


        /**
         *备注
         */
        @Size(max = 200, message = "发票备注最大200字符")
        private String remark;

        /**
         * 纳税登记号
         */
        @Size(max = 100, message = "纳税登记号最大100字符")
        private String taxRegisterCode;

        /**
         * 开票联系电话
         */
        @Size(max = 100, message = "开票联系电话100字符")
        private String invoiceTel;

        /**
         * 开票通讯地址
         */
        @Size(max = 500, message = "开票通讯地址最大500字符")
        private String invoiceAddress;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * id
         */
        private String id;
        /**
         * mainId
         */
        private String mainId;
        /**
         * 客户编号
         */
        private String code;
        /**
         * 客户名称
         */
        private String name;
        /**
         * 发票抬头
         */
        @NotBlank(message = "发票抬头不能为空")
        @Size(max = 50, message = "发票抬头最大50字符")
        private String head;

        /**
         * 类型
         */
        @StateEnumValue(strValues = {"valueAddedTax","invoice"},message = "发票类型有误")
        private String type;
        private String typeName;

        /**
         * 银行名称
         */
        @NotBlank(message = "开户银行不能为空")
        @Size(max = 50, message = "开户银行最大50字符")
        private String bankName;

        /**
         * 银行账号
         */
        @NotBlank(message = "银行账号不能为空")
        @Size(max = 50, message = "银行账号最大50字符")
        private String bankAccount;



        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;
        private String isDefaultName;


        /**
         *备注
         */
        @Size(max = 200, message = "发票备注最大200字符")
        private String remark;

        /**
         * 纳税登记号
         */
        @Size(max = 100, message = "纳税登记号最大100字符")
        private String taxRegisterCode;

        /**
         * 开票联系电话
         */
        @Size(max = 100, message = "开票联系电话100字符")
        private String invoiceTel;

        /**
         * 开票通讯地址
         */
        @Size(max = 500, message = "开票通讯地址最大500字符")
        private String invoiceAddress;

    }
}
