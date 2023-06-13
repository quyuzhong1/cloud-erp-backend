package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CustomerGroupDTO
 * @Description TODO
 * @Date 2023-05-11 17:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomerGroupDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * 名称
         */
        @NotBlank(message = "分组名称不能为空")
        @Size(max = 50, message = "分组名称最大50字符")
        private String name;
    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * 名称
         */
        private String name;


        /**
         * 删除禁用状态
         * true 就是不能删除 被客户引用
         * false 就是可以删除
         */
        private Boolean disabled;
    }
}
