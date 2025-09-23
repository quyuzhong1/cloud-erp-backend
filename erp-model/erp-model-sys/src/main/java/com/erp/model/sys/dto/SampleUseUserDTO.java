package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname SampleUseUserDTO
 * @Date 2025-01-27 16:14
 * @Created by Lambda
 */
public class SampleUseUserDTO implements Serializable {

    /**
     * 添加或者修改示例用户
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddOrUpdateDTO {

        private String id;

        @NotBlank(message = "用户名称不能为空")
        @Size(max = 50, message = "用户名称最大50字符")
        private String name;

        /**
         * 是否禁用
         */
        private Boolean disabled;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 用户名称
         */
        private String name;

        /**
         * 是否禁用
         */
        private Boolean disabled;

    }

    /**
     * 查询参数
     */
    @Data
    @NoArgsConstructor
    public static class QueryDTO {

        /**
         * 用户名称（支持模糊查询）
         */
        private String name;

    }

}
