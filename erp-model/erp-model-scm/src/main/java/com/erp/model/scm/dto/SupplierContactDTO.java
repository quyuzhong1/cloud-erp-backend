package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 供应商联系信息
 * @author Lambda
 * @Classname SupplierContactDTO
 * @Description TODO
 * @Date 2023-03-17 14:39
 * @Created by yl
 */
public class SupplierContactDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO{


        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        private String person;

        /**
         * 职位
         */
        private String position;

        /**
         * 电话
         */
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MOBILE,message = "电话格式有误")
        private String telNumber;

        /**
         * 邮箱
         */
        @Size(max = 50,message = "邮箱最大50字符")
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
