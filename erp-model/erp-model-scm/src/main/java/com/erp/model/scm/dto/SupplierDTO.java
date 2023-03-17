package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商信息
 *
 * @author yl
 * @Classname SupplierDTO
 * @Description TODO
 * @Date 2023-03-15 16:35
 * @Created by yl
 */
public class SupplierDTO implements Serializable {


    /**
     * 供应商基础信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierBaseDTO{

        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 50, message = "最大50字符")
        private String name;


        /**
         * 分类id
         */
        @NotBlank(message = "分类id不能为空")
        private String categoryId;

        /**
         * 等级id
         */
        @NotBlank(message = "等级id")
        private String gradeId;


        /**
         * 采购员id
         */
        private String purchaseUserId;


        /**
         * 公司地址
         */
        @Size(max = 100, message = "最大50字符")
        private String companyAddress;


        /**
         * 公司网址
         */
        @Size(max = 100, message = "最大50字符")
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.URL,message = "网址有误")
        private String companyWebsite;


        /**
         * 生命周期
         */
        @NotBlank(message = "阶段不能为空")
        private String phase;


        /**
         * 结算付款方式
         */
        private String payMethod;

        /**
         * 结算付款币种
         */
        private String payCurrency;


        /**
         * true 启用   false 禁用
         */
        private Boolean openStatus;

    }

    /**
     * 供应商联系信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierContactDTO{

        /**
         * 表id
         */
        private String id;


        /**
         * 联系人
         */
        private String person;

        /**
         * 职位
         */
        private String position;

        /**
         * 电话
         */
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MOBILE,message = "电话格式有误")
        private String phone;

        /**
         * 邮箱
         */
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MAILBOX,message = "邮箱格式有误")
        private String email;

        /**
         * 是否默认 true  是
         */
        private Boolean isDefault;

        /**
         * 开启状态 true 开启
         */
        private Boolean openStatus;

        /**
         * 备注信息
         */
        private String remark;

    }

    /**
     * 供应商结算信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierAccountDTO{

        /**
         * 收款方
         */
        private String payee;

        /**
         * 银行名称
         */
        @Size(max = 50,message = "最大50字符")
        private String bankName;

        /**
         * 银行账号
         */
        @Size(max = 20,message = "卡号最大20字符")
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.BANK_CARD_NO,message = "银行卡号有误")
        private String bankAccount;


        /**
         * 支行
         */
        private String bankSubbranch;

        /**
         * 支付方式
         */
        private String payMethod;

        /**
         * 备注
         */
        @Size(max = 255,message = "最大255字符")
        private String remark;

    }


    /**
     * 供应商资质信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierCredentialDTO{

        /**
         * 表id
         */
        private String id;

        /**
         * 名称
         */
        @NotBlank(message = "资质名称不能为空")
        @Size(max = 50,message = "最大50字符")
        private String name;


        /**
         * 有效时间
         */
        private LocalDate effectiveDate;


        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 备注
         */
        @Size(max = 255,message = "最大255字符")
        private String remark;


        /**
         * 资质附件url
         */
        private List<String> credentialAttachmentList;

    }

    /**
     * 供应商等级信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public  static class SupplierGradeDTO{
        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        @NotBlank(message = "等级名称不能为空")
        @Size(max = 50,message = "名称最大50字符")
        private String name;
    }



}
