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
    public static final String DISABLED = "disabled";
    public static final String SORT = "sort";
}
