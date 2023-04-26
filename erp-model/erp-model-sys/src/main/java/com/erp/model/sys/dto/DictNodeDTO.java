package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Lambda
 * @Classname DictNodeDTO
 * @Description TODO
 * @Date 2023-04-26 14:25
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictNodeDTO {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        @Size(max = 20, message = "节点key不能超过20字符")
        @NotBlank(message = "节点key不能为空")
        private String nodeKey;

        @Size(max = 30, message = "节点名称不能超过30字符")
        @NotBlank(message = "节点名称不能为空")
        private String nodeName;


        @NotBlank(message = "业务模块不能为空")
        private String module;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String nodeCode;


        private String nodeName;


        private String businessType;

        private String id;
    }
}
