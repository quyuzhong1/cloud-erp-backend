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

        /**
         * 节点key
         */
        private String nodeKey;

        /**
         * 类型
         */
        private String type;

        /**
         * 类型名
         */
        private String typeName;

        /**
         * 请求的url
         */
        private String url;

        /**
         * 请求方式
         */
        private String requestMethod;

        /**
         * 请求参数
         */
        private String requestParam;

        /**
         * 值
         */
        private String valueField;


        /**
         * label
         */
        private String labelField;


    }

}
