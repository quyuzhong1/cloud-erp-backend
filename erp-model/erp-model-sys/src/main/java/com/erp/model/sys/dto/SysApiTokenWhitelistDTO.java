package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API Token 接口白名单配置请求响应实体
 */
@Data
@NoArgsConstructor
public class SysApiTokenWhitelistDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {

        /**
         * 白名单路径匹配模式
         */
        @NotBlank(message = "接口路径不能为空")
        @Size(max = 500, message = "接口路径最大长度不能超过500位")
        private String pathPattern;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO implements Serializable {

        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 白名单路径匹配模式
         */
        @NotBlank(message = "接口路径不能为空")
        @Size(max = 500, message = "接口路径最大长度不能超过500位")
        private String pathPattern;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {

        private String id;

        /**
         * 白名单路径匹配模式
         */
        private String pathPattern;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }
}
