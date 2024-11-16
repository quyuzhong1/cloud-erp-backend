package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 供应商联系信息
 * @author Lambda
 * @Classname SupplierContactDTO

 * @Date 2023-03-17 14:39
 * @Created by yl
 */
public class SupplierContactDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable{



        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        @Size(max =50 ,message = "联系人最大50字符")
        private String person;

        /**
         * 职位
         */
        @Size(max =50 ,message = "联系人职务最大50字符")
        private String position;

        /**
         * 电话
         */
       // @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MOBILE,message = "电话格式有误")
        @Size(max =20 ,message = "联系人电话最大20字符")
        @NotBlank(message = "联系电话必填")
        private String telNumber;

        /**
         * 邮箱
         */
        @Size(max = 30,message = "邮箱最大30字符")
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MAILBOX,message = "邮箱格式有误")
        private String email;

        /**
         * 是否默认 true  是
         */
        private Boolean isDefault;

        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;
        /**
         * SRM协同 true 否 false 是
         */
        private Boolean srmDisabled;

        /**
         * 备注信息
         */
        @Size(max = 250,message = "联系人备注最大250个字符")
        private String remark;

    }


    @Data
    @NoArgsConstructor
    public static class ImportAddDTO{


        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String supplierName;

        /**
         * 联系人
         */
        @Size(max =50 ,message = "联系人最大50字符")
        private String person;

        /**
         * 职位
         */
        @Size(max =50 ,message = "联系人职务最大50字符")
        private String position;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 邮箱
         */
        @Size(max = 30,message = "邮箱最大30字符")
        private String email;

        /**
         * 是否默认 true  是
         */
        private Boolean isDefault;

        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 备注信息
         */
        private String remark;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO{
        /**
         * 主表id
         */
        private String id;
    }


    @Data
    @NoArgsConstructor
    public static class DropDownDTO {
        /**
         * 主表id
         */
        private String id;

        /**
         * 联系人
         */
        private String person;

        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 是否默认 true  是
         */
        private Boolean isDefault;

        /**
         * 电话
         */
        private String telNumber;
    }




}
