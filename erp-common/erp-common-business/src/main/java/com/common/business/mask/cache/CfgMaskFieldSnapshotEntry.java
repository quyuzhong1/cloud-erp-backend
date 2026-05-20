package com.common.business.mask.cache;

import com.common.business.mask.MaskStrategy;
import com.common.business.mask.protect.MaskProtectBinding;
import com.common.business.mask.protect.MaskProtectMode;
import com.common.business.mask.protect.MaskProtectVerifyMode;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置表 {@code cfg_mask_field} 单行的 Redis 快照视图
 *
 * <p>由 {@link CfgMaskFieldLocalCache#apply} 从 {@link CfgMaskFieldFullCacheDTO} 转换写入快照，
 * 字段全部 final-after-init 风格：构造完后不允许修改，便于安全发布。</p>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgMaskFieldSnapshotEntry {

    private String classPath;
    private String fieldName;
    private MaskStrategy strategy;
    private String regex;
    private String replacement;
    private String permission;
    private Integer sort;

    /**
     * "不可见"语义开关：true=脱敏后再置 null（隐藏整字段），false=按 strategy 脱敏展示
     */
    private boolean hideWhenMasked;

    /**
     * 是否开启脱敏回显保护。
     */
    private boolean valueProtectEnabled;

    /**
     * 保存接口入参 DTO 类路径，兼容单绑定简写。
     */
    private String protectParamClassPath;

    /**
     * 保存接口入参 DTO 字段名，兼容单绑定简写。
     */
    private String protectParamFieldName;

    /**
     * 读侧 VO 记录 ID 字段名，保留用于配置兼容；写保护恢复以保存入参绑定的 recordId 为准。
     */
    private String protectRecordIdField;

    /**
     * 保存接口入参 DTO 中用于定位记录主键的字段名，默认与 protectRecordIdField 一致。
     */
    private String protectParamRecordIdField;

    /**
     * 读侧 VO 版本 / 更新时间字段名，保留用于配置兼容。
     */
    private String protectVersionField;

    /**
     * 保存接口入参 DTO 版本 / 更新时间字段名，保留用于配置兼容。
     */
    private String protectParamVersionField;

    /**
     * 回显保护安全校验方式。
     */
    private MaskProtectVerifyMode protectVerifyMode;

    /**
     * DB_VALUE_COMPARE 模式下查询当前值的表名。
     */
    private String protectTableName;

    /**
     * DB_VALUE_COMPARE 模式下记录 ID 列名，默认 id。
     */
    private String protectRecordIdColumn;

    /**
     * DB_VALUE_COMPARE 模式下敏感字段原值列名。
     */
    private String protectValueColumn;

    /**
     * DB_VALUE_COMPARE 模式下逻辑删除列名，空表示不追加逻辑删除条件。
     */
    private String protectDeletedColumn;

    /**
     * 保存接口入参 DTO 绑定列表；支持一个读侧 VO 字段保护多个 UpdateDTO。
     */
    private List<MaskProtectBinding> protectParamBindings = new ArrayList<>();

    /**
     * 保留兼容字段；当前 DB 当前值恢复流程不使用。
     */
    private Integer protectTtlSeconds;

    /**
     * 保留兼容字段；当前写保护不再按提交值是否像脱敏值来决定是否保护。
     */
    private String protectMaskedValueRegex;

    /**
     * 回显保护模式。
     */
    private MaskProtectMode protectMode;
}
