package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字段脱敏配置请求/响应实体
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class CfgMaskFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "类全名不能为空")
        private String classPath;

        @NotBlank(message = "字段名不能为空")
        private String fieldName;

        /**
         * 脱敏策略，参见 com.common.business.mask.MaskStrategy
         */
        @NotBlank(message = "脱敏策略不能为空")
        private String strategy;

        private String customRegex;
        private String customReplace;
        private String permissionCode;
        /**
         * "不可见"语义开关：true=脱敏后置 null（隐藏），false=按 strategy 脱敏展示
         */
        private Boolean hideWhenMasked = Boolean.FALSE;
        private Boolean enabled = Boolean.TRUE;
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "id不能为空")
        private String id;

        @NotNull(message = "version不能为空")
        private Integer version;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String classPath;
        private String fieldName;
        private String strategy;
        private String permissionCode;
        private Boolean hideWhenMasked;
        private Boolean enabled;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private String classPath;
        private String fieldName;
        private String strategy;
        private String customRegex;
        private String customReplace;
        private String permissionCode;
        private Boolean hideWhenMasked;
        private Boolean enabled;
        private String remark;
        private Integer version;
        private LocalDateTime createTime;
        private String createUserName;
        private LocalDateTime updateTime;
        private String updateUserName;
    }
}
