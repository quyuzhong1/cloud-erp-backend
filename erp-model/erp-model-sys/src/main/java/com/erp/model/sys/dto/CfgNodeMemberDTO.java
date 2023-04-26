package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CfgNodeMemberDTO
 * @Description TODO
 * @Date 2023-04-26 17:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CfgNodeMemberDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        private String id;

        private String nodeKey;

        private String type;

        private String typeName;

        private String url;

        private String requestMethod;

        private String requestParam;


        private String valueField;

        private String labelField;


    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {


        private String nodeKey;

        private String type;

        private String typeName;

        private String url;

        private String requestMethod;

        private String requestParam;


        private String valueField;

        private String labelField;


    }

}
