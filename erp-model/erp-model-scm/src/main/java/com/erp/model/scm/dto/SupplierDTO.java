package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
     * 供应商基础添加信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddDTO extends SupplierBaseDTO{



        /**
         * 供应商联系信息
         */
        @Valid
        private List<SupplierContactDTO.AddDTO>  contactList;


        /**
         * 供应商银行账户信息
         */
        @Valid
        private List<SupplierAccountDTO.AddDTO>  bankAccountList;


        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.AddDTO> credentialList;



    }


    /**
     * 供应商基础信息
     */
    @Data
    @NoArgsConstructor
    @Valid
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
        @NotBlank(message = "等级id不能为空")
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
         * 结算付款方式
         */
        @NotBlank(message = "结算方式不能为空")
        private String payMethodId;

        /**
         * 结算付款币种
         */
        @NotBlank(message = "结算币种不能为空")
        private String payCurrency;


        /**
         * true 启用   false 禁用
         */
        @NotNull(message = "启用状态不能为空")
        private Boolean openStatus;

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
