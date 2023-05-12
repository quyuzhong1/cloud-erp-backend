package com.erp.model.oms.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CustomerContactDTO
 * @Description TODO
 * @Date 2023-05-10 16:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomerContactDTO implements Serializable {


    /**
     * 新增加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50, message = "联系人最大50字符")
        private String person;

        /**
         * 职位
         */
        @Size(max = 50, message = "职位最大50字符")
        private String position;

        /**
         * 联系电话
         */
        @Size(max = 20, message = "联系电话最大20字符")
        private String telNumber;

        /**
         * 邮箱
         */
        @Size(max = 50, message = "邮箱最大50字符")
        @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MAILBOX,message = "邮箱格式有误")
        private String email;


        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;


        /**
         * 是否禁用
         * true 是
         * false 不是
         */
        private Boolean disabled;

        /**
         *备注
         */
        @Size(max = 200, message = "邮箱最大200字符")
        private Boolean remark;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


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
         * 联系电话
         */
        private String telNumber;

        /**
         * 邮箱
         */
        private String email;


        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;


        /**
         * 是否禁用
         * true 是
         * false 不是
         */
        private Boolean disabled;

        /**
         *备注
         */
        private Boolean remark;


    }
}
