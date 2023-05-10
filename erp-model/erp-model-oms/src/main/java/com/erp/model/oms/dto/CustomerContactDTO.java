package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
