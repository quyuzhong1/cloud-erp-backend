package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname FindThirdUserInfo

 * @Date 2022-07-26 10:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindThirdUserDTO implements Serializable {

    private String code;

    private String thirdType;

    private String[] mobiles;


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class UserParamsDTO {
        private String[] emails;

        private String[] mobiles;

        private String[] userIds;

        private String[] departmentIds;

        private String userIdType;

        private String departmenetIdType;
    }



}
