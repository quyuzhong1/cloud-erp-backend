package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname OrderCategoryDetailDTO
 * @Date 2023-08-25 14:48
 * @Created by yl
 */
public class OrderCategoryDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 组名
         */
        @NotBlank(message = "分类不能为空")
        @Size(max = 20, message = "分类最大20字符")
        private String name;


    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        private String id;

        /**
         * 组名
         */
        @NotBlank(message = "分类不能为空")
        @Size(max = 20, message = "分类最大20字符")
        private String name;


    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        /**
         * 明细名
         */
        private String name;


        private Boolean disabled;


    }



}
