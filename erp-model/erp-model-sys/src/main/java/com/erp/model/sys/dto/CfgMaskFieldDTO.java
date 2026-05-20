package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        /**
         * 是否开启脱敏值回显保护。
         */
        private Boolean valueProtectEnabled = Boolean.FALSE;
        /**
         * 保存接口入参 DTO 类路径，兼容单绑定简写。
         */
        private String protectParamClassPath;
        /**
         * 保存接口入参 DTO 字段名，默认与 fieldName 一致。
         */
        private String protectParamFieldName;
        /**
         * 记录 ID 字段名，默认 id。
         */
        private String protectRecordIdField = "id";
        /**
         * 保存接口入参 DTO 记录 ID 字段名，默认与 protectRecordIdField 一致。
         */
        private String protectParamRecordIdField;
        /**
         * 版本 / 更新时间字段名，推荐 version 或 updateTime。
         */
        private String protectVersionField;
        /**
         * 保存接口入参 DTO 版本 / 更新时间字段名，默认与 protectVersionField 一致。
         */
        private String protectParamVersionField;
        /**
         * 回显保护安全校验方式：DB_VALUE_COMPARE / PARAM_VERSION / REJECT。
         */
        private String protectVerifyMode = "DB_VALUE_COMPARE";
        /**
         * DB_VALUE_COMPARE 模式下查询当前值的表名。
         */
        private String protectTableName;
        /**
         * DB_VALUE_COMPARE 模式下记录 ID 列名，默认 id。
         */
        private String protectRecordIdColumn = "id";
        /**
         * DB_VALUE_COMPARE 模式下敏感字段原值列名。
         */
        private String protectValueColumn;
        /**
         * DB_VALUE_COMPARE 模式下逻辑删除列名，空表示不追加逻辑删除条件。
         */
        private String protectDeletedColumn = "is_deleted";
        /**
         * 保存接口入参 DTO 绑定列表，支持一个读侧 VO 字段保护多个保存 DTO。
         */
        private List<ProtectParamBindingDTO> protectParamBindings = new ArrayList<>();
        /**
         * 回显保护 Redis TTL（秒）。
         */
        private Integer protectTtlSeconds = 300;
        /**
         * 识别提交值是否为脱敏占位的正则，空则使用默认识别规则。
         */
        private String protectMaskedValueRegex;
        /**
         * 回显保护模式：RESTORE_ORIGINAL / REJECT。
         */
        private String protectMode = "RESTORE_ORIGINAL";
        private Boolean disabled = Boolean.FALSE;
        private Integer sort = 0;
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
        private Boolean valueProtectEnabled;
        private String protectParamClassPath;
        private String protectParamFieldName;
        private String protectParamRecordIdField;
        private String protectParamVersionField;
        private String protectParamBindings;
        private String protectVerifyMode;
        private String protectTableName;
        private String protectRecordIdColumn;
        private String protectValueColumn;
        private String protectDeletedColumn;
        private Boolean disabled;
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
        private Boolean valueProtectEnabled;
        private String protectParamClassPath;
        private String protectParamFieldName;
        private String protectRecordIdField;
        private String protectParamRecordIdField;
        private String protectVersionField;
        private String protectParamVersionField;
        private String protectParamBindings;
        private String protectVerifyMode;
        private String protectTableName;
        private String protectRecordIdColumn;
        private String protectValueColumn;
        private String protectDeletedColumn;
        private Integer protectTtlSeconds;
        private String protectMaskedValueRegex;
        private String protectMode;
        private Boolean disabled;
        private Integer sort;
        private String remark;
        private Integer version;
        private LocalDateTime createTime;
        private String createUserName;
        private LocalDateTime updateTime;
        private String updateUserName;
    }

    @Data
    @NoArgsConstructor
    public static class ProtectParamBindingDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String paramClassPath;
        private String paramFieldName;
        private String paramRecordIdField;
        private String paramVersionField;
    }
}
