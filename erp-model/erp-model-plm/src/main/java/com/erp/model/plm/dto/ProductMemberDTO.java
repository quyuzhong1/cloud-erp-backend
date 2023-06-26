package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
        private Integer count=0;



        /**
         * 成员id
         */
        private String memberId;

        /**
         * 成员名
         */
        private String memberName;

        /**
         * 成员信息
         */
        @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        private List<TaskRefDTO> childrenList;
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
