package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname UserInfoDTO
 * @Description TODO
 * @Date 2023-08-07 9:23
 * @Created by yl
 */
public class UserInfoDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class BusinessOperationUserDTO {

        /**
         * 用户id
         */
        private String userId;

        /**
         * 金蝶code
         */
        private String code;

        /**
         * 金蝶id
         */
        private String syncKingdeeId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 电话
         */
        private String mobile;

        /**
         * 是否自己
         */
        private Integer isMyState;
        /**
         * 部门id
         */
        private String departmentId;
        /**
         * 部门名
         */
        private String departmentName;

        /**
         * 是否禁用
         * true 禁用
         */
        private Boolean disabled;

    }
}
