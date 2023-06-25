package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductMemberDTO

 * @Date 2023-06-20 11:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductMemberDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class TaskRefDTO {

        /**
         * 人数
         */
        private Integer count;

        /**
         * 成员类型
         */
        private String memberType;

        /**
         * 成员类型名
         */
        private String memberTypeName;

        /**
         * 成员信息
         */
        private List<MemberDTO> memberList;
    }


    @Data
    @NoArgsConstructor
    public static class MemberDTO {

        /**
         * 用户id
         */
        private String userId;

        /**
         * 用户名
         */
        private String userName;

    }

}
