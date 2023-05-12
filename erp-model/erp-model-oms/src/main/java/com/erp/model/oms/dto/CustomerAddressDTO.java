package com.erp.model.oms.dto;

import com.common.core.anno.RegularValid;
import com.common.core.anno.StateEnumValue;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CustomerAddessDTO
 * @Description TODO
 * @Date 2023-05-10 17:05
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomerAddressDTO implements Serializable {

    /**
     * 增加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 地址
         */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 50, message = "详细地址最大50字符")
        private String address;

        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50, message = "联系人最大50字符")
        private String person;

        /**
         * 地址类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=AddressType
         */
        @StateEnumValue(strValues = {"forwarder","deliver","company"},message = "地址类型有误")
        private String type;

        /**
         * 电话
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
         * 备注
         */
        @Size(max = 200, message = "联系地址备注最大200字符")
        private String remark;


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
         * 地址
         */
        private String address;

        /**
         * 联系人
         */
        private String contactPerson;

        /**
         * 地址类型
         */
        private String addressType;

        /**
         * 电话
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
         * 备注
         */
        private String remark;

    }
}
