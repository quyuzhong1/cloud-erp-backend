package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
        private Boolean remark;


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
        private Boolean remark;

    }
}
