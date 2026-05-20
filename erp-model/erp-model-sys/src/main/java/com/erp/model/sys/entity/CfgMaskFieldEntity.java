package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 字段脱敏配置表
 *
 * <p>配置变更后删除 Redis 全量缓存，业务节点按 cache-aside 回源最新规则。
 * 同一字段的"运维配置"覆盖"@Mask 注解"，运维侧无需改代码即可启停脱敏。</p>
 *
 * @author cloud-erp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_mask_field")
public class CfgMaskFieldEntity extends BaseEntity<CfgMaskFieldEntity> {

    private static final long serialVersionUID = 1L;

    /**
     * 类全限定名，例：com.erp.model.user.dto.UserShowVO
     */
    @TableField("class_path")
    private String classPath;

    /**
     * Java 字段名（驼峰），例：mobile
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 脱敏策略：与 com.common.business.mask.MaskStrategy 同名
     * 例：PHONE / ID_CARD / BANK_CARD / EMAIL / NAME / ADDRESS / AMOUNT / PASSWORD / ALL / CUSTOM
     */
    @TableField("strategy")
    private String strategy;

    /**
     * 自定义正则（strategy=CUSTOM 时生效）
     */
    @TableField("custom_regex")
    private String customRegex;

    /**
     * 自定义替换串（strategy=CUSTOM 时生效，默认 ***）
     */
    @TableField("custom_replace")
    private String customReplace;

    /**
     * 看明文所需权限码（拥有该权限的用户不脱该字段）。
     * <p>多行可共享同一 permission_code 形成"业务字段概念组"，一次授权对该组所有字段生效。</p>
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * "不可见"语义开关：
     * <ul>
     *   <li>false（默认）：按 {@link #strategy} 脱敏展示（如 138****5678）</li>
     *   <li>true：脱敏后再把整字段置 null（前端看不到该字段）</li>
     * </ul>
     * <p>仅在用户无明文权限时生效；有明文权限时本开关被忽略。</p>
     */
    @TableField("hide_when_masked")
    private Boolean hideWhenMasked;

    /**
     * 是否开启脱敏值回显保护。
     */
    @TableField("value_protect_enabled")
    private Boolean valueProtectEnabled;

    /**
     * 保存接口入参 DTO 类路径，兼容单绑定简写。
     */
    @TableField("protect_param_class_path")
    private String protectParamClassPath;

    /**
     * 保存接口入参 DTO 字段名，默认与 {@link #fieldName} 一致。
     */
    @TableField("protect_param_field_name")
    private String protectParamFieldName;

    /**
     * 读侧 VO 记录 ID 字段名，保留用于配置兼容。
     */
    @TableField("protect_record_id_field")
    private String protectRecordIdField;

    /**
     * 保存接口入参 DTO 记录 ID 字段名，默认与 protectRecordIdField 一致。
     */
    @TableField("protect_param_record_id_field")
    private String protectParamRecordIdField;

    /**
     * 读侧 VO 版本 / 更新时间字段名，保留用于配置兼容。
     */
    @TableField("protect_version_field")
    private String protectVersionField;

    /**
     * 保存接口入参 DTO 版本 / 更新时间字段名，保留用于配置兼容。
     */
    @TableField("protect_param_version_field")
    private String protectParamVersionField;

    /**
     * 回显保护安全校验方式：DB_VALUE_COMPARE / PARAM_VERSION / REJECT。
     */
    @TableField("protect_verify_mode")
    private String protectVerifyMode;

    /**
     * DB_VALUE_COMPARE 模式下查询当前值的表名。
     */
    @TableField("protect_table_name")
    private String protectTableName;

    /**
     * DB_VALUE_COMPARE 模式下记录 ID 列名，默认 id。
     */
    @TableField("protect_record_id_column")
    private String protectRecordIdColumn;

    /**
     * DB_VALUE_COMPARE 模式下敏感字段原值列名。
     */
    @TableField("protect_value_column")
    private String protectValueColumn;

    /**
     * DB_VALUE_COMPARE 模式下逻辑删除列名，空表示不追加逻辑删除条件。
     */
    @TableField("protect_deleted_column")
    private String protectDeletedColumn;

    /**
     * 多个保存入参 DTO 绑定，JSON 数组格式。
     */
    @TableField("protect_param_bindings")
    private String protectParamBindings;

    /**
     * 保留兼容字段；当前 DB 当前值恢复流程不使用。
     */
    @TableField("protect_ttl_seconds")
    private Integer protectTtlSeconds;

    /**
     * 保留兼容字段；当前写保护不再按提交值是否像脱敏值来决定是否保护。
     */
    @TableField("protect_masked_value_regex")
    private String protectMaskedValueRegex;

    /**
     * 回显保护模式：RESTORE_ORIGINAL / SET_NULL / REJECT。
     */
    @TableField("protect_mode")
    private String protectMode;

    /**
     * 是否禁用（true 即视为该配置不存在，注解仍会生效）
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 多条规则命中同一字段时的执行顺序，越小越先执行
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public static final String CLASS_PATH = "class_path";
    public static final String FIELD_NAME = "field_name";
    public static final String STRATEGY = "strategy";
    public static final String PERMISSION_CODE = "permission_code";
    public static final String HIDE_WHEN_MASKED = "hide_when_masked";
    public static final String VALUE_PROTECT_ENABLED = "value_protect_enabled";
    public static final String PROTECT_PARAM_CLASS_PATH = "protect_param_class_path";
    public static final String PROTECT_PARAM_FIELD_NAME = "protect_param_field_name";
    public static final String PROTECT_RECORD_ID_FIELD = "protect_record_id_field";
    public static final String PROTECT_PARAM_RECORD_ID_FIELD = "protect_param_record_id_field";
    public static final String PROTECT_VERSION_FIELD = "protect_version_field";
    public static final String PROTECT_PARAM_VERSION_FIELD = "protect_param_version_field";
    public static final String PROTECT_VERIFY_MODE = "protect_verify_mode";
    public static final String PROTECT_TABLE_NAME = "protect_table_name";
    public static final String PROTECT_RECORD_ID_COLUMN = "protect_record_id_column";
    public static final String PROTECT_VALUE_COLUMN = "protect_value_column";
    public static final String PROTECT_DELETED_COLUMN = "protect_deleted_column";
    public static final String PROTECT_PARAM_BINDINGS = "protect_param_bindings";
    public static final String PROTECT_TTL_SECONDS = "protect_ttl_seconds";
    public static final String PROTECT_MASKED_VALUE_REGEX = "protect_masked_value_regex";
    public static final String PROTECT_MODE = "protect_mode";
    public static final String DISABLED = "disabled";
    public static final String SORT = "sort";
}
