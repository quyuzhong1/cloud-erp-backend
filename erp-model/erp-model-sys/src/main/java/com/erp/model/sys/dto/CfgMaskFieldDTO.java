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
         * RESTORE_ORIGINAL 模式下查询当前值的表名。
         */
        private String protectTableName;
        /**
         * RESTORE_ORIGINAL 模式下记录 ID 列名，默认 id。
         */
        private String protectRecordIdColumn = "id";
        /**
         * RESTORE_ORIGINAL 模式下敏感字段原值列名。
         */
        private String protectValueColumn;
        /**
         * RESTORE_ORIGINAL 模式下逻辑删除列名，空表示不追加逻辑删除条件。
         */
        private String protectDeletedColumn = "is_deleted";
        /**
         * 保存接口入参 DTO 绑定列表，支持一个读侧 VO 字段保护多个保存 DTO。
         */
        private List<ProtectParamBindingDTO> protectParamBindings = new ArrayList<>();
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
        private String protectParamBindings;
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
        private String protectParamBindings;
        private String protectTableName;
        private String protectRecordIdColumn;
        private String protectValueColumn;
        private String protectDeletedColumn;
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
    }
}
