package com.common.business.mask.core;

import com.common.business.mask.MaskStrategy;
import com.common.business.mask.protect.MaskProtectBinding;
import com.common.business.mask.protect.MaskProtectMode;
import com.common.business.mask.protect.MaskProtectVerifyMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单个字段的脱敏元数据快照
 *
 * <p>由 {@link MaskClassDescriptorRegistry} 在首次扫描类时构造并永久缓存（运行期不变）。
 * 已合并：注解 + {@code cfg_mask_field} 配置表（配置表覆盖注解）。</p>
 *
 * @author cloud-erp
 */
public final class MaskFieldDescriptor {

    private final Field field;
    private final MaskStrategy strategy;
    private final String regex;
    private final String replacement;
    private final String permission;
    private final boolean recursive;
    private final boolean keepEmpty;
    /**
     * "不可见"语义开关：true 时未豁免明文用户脱敏后再置 null（隐藏整字段）
     */
    private final boolean hideWhenMasked;

    /**
     * 是否开启脱敏回显保护。
     */
    private final boolean valueProtectEnabled;

    /**
     * 保存接口入参 DTO 类路径，兼容单绑定简写。
     */
    private final String protectParamClassPath;

    /**
     * 保存接口入参 DTO 字段名，兼容单绑定简写。
     */
    private final String protectParamFieldName;

    /**
     * 保存接口入参 DTO 绑定列表。
     */
    private final List<MaskProtectBinding> protectParamBindings;

    /**
     * 读侧对象记录 ID 字段名，保留用于配置兼容。
     */
    private final String protectRecordIdField;

    /**
     * 读侧对象版本 / 更新时间字段名，保留用于配置兼容。
     */
    private final String protectVersionField;

    /**
     * 回显保护安全校验方式。
     */
    private final MaskProtectVerifyMode protectVerifyMode;

    /**
     * DB_VALUE_COMPARE 模式下查询当前值的表名。
     */
    private final String protectTableName;

    /**
     * DB_VALUE_COMPARE 模式下记录 ID 列名。
     */
    private final String protectRecordIdColumn;

    /**
     * DB_VALUE_COMPARE 模式下敏感字段原值列名。
     */
    private final String protectValueColumn;

    /**
     * DB_VALUE_COMPARE 模式下逻辑删除列名，空表示不追加逻辑删除条件。
     */
    private final String protectDeletedColumn;

    /**
     * 保留兼容字段；当前 DB 当前值恢复流程不使用。
     */
    private final Integer protectTtlSeconds;

    /**
     * 保留兼容字段；当前写保护不再按提交值是否像脱敏值来决定是否保护。
     */
    private final String protectMaskedValueRegex;

    /**
     * 回显保护模式。
     */
    private final MaskProtectMode protectMode;

    /**
     * 多条配置规则作用在同一字段时的执行顺序，越小越先执行。
     */
    private final int sort;

    /**
     * 是否是"嵌套对象 / 集合 / Map" —— 需要继续向下递归
     * （即使该字段没有 @Mask，但其类型可能内部包含被 @Mask 的字段）
     */
    private final boolean container;

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container) {
        this(field, strategy, regex, replacement, permission, recursive, keepEmpty,
                hideWhenMasked, container, 0);
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort) {
        this(field, strategy, regex, replacement, permission, recursive, keepEmpty,
                hideWhenMasked, container, sort, false, "", "", null, "", MaskProtectMode.REJECT);
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort,
                               boolean valueProtectEnabled, String protectRecordIdField,
                               String protectVersionField, Integer protectTtlSeconds,
                               String protectMaskedValueRegex, MaskProtectMode protectMode) {
        this(field, strategy, regex, replacement, permission, recursive, keepEmpty,
                hideWhenMasked, container, sort, valueProtectEnabled, "", "",
                protectRecordIdField, protectVersionField, protectTtlSeconds,
                protectMaskedValueRegex, protectMode, Collections.emptyList());
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort,
                               boolean valueProtectEnabled, String protectParamClassPath,
                               String protectParamFieldName, String protectRecordIdField,
                               String protectVersionField, Integer protectTtlSeconds,
                               String protectMaskedValueRegex, MaskProtectMode protectMode) {
        this(field, strategy, regex, replacement, permission, recursive, keepEmpty,
                hideWhenMasked, container, sort, valueProtectEnabled, protectParamClassPath,
                protectParamFieldName, protectRecordIdField, protectVersionField,
                protectTtlSeconds, protectMaskedValueRegex, protectMode,
                singletonBinding(protectParamClassPath, protectParamFieldName, protectRecordIdField,
                        protectVersionField));
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort,
                               boolean valueProtectEnabled, String protectParamClassPath,
                               String protectParamFieldName, String protectRecordIdField,
                               String protectVersionField, Integer protectTtlSeconds,
                               String protectMaskedValueRegex, MaskProtectMode protectMode,
                               List<MaskProtectBinding> protectParamBindings) {
        this(field, strategy, regex, replacement, permission, recursive, keepEmpty,
                hideWhenMasked, container, sort, valueProtectEnabled, protectParamClassPath,
                protectParamFieldName, protectRecordIdField, protectVersionField,
                MaskProtectVerifyMode.PARAM_VERSION, "", "", "", "",
                protectTtlSeconds, protectMaskedValueRegex, protectMode, protectParamBindings);
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort,
                               boolean valueProtectEnabled, String protectParamClassPath,
                               String protectParamFieldName, String protectRecordIdField,
                               String protectVersionField, MaskProtectVerifyMode protectVerifyMode,
                               String protectTableName, String protectRecordIdColumn,
                               String protectValueColumn, String protectDeletedColumn,
                               Integer protectTtlSeconds, String protectMaskedValueRegex,
                               MaskProtectMode protectMode, List<MaskProtectBinding> protectParamBindings) {
        this.field = field;
        this.strategy = strategy;
        this.regex = regex == null ? "" : regex;
        this.replacement = replacement == null ? "***" : replacement;
        this.permission = permission == null ? "" : permission;
        this.recursive = recursive;
        this.keepEmpty = keepEmpty;
        this.hideWhenMasked = hideWhenMasked;
        this.container = container;
        this.sort = sort;
        this.valueProtectEnabled = valueProtectEnabled;
        this.protectParamClassPath = protectParamClassPath == null ? "" : protectParamClassPath;
        this.protectParamFieldName = protectParamFieldName == null || protectParamFieldName.isEmpty()
                ? field.getName() : protectParamFieldName;
        this.protectRecordIdField = protectRecordIdField == null ? "" : protectRecordIdField;
        this.protectVersionField = protectVersionField == null ? "" : protectVersionField;
        this.protectVerifyMode = protectVerifyMode == null ? MaskProtectVerifyMode.PARAM_VERSION : protectVerifyMode;
        this.protectTableName = protectTableName == null ? "" : protectTableName;
        this.protectRecordIdColumn = protectRecordIdColumn == null ? "" : protectRecordIdColumn;
        this.protectValueColumn = protectValueColumn == null ? "" : protectValueColumn;
        this.protectDeletedColumn = protectDeletedColumn == null ? "" : protectDeletedColumn;
        this.protectTtlSeconds = protectTtlSeconds;
        this.protectMaskedValueRegex = protectMaskedValueRegex == null ? "" : protectMaskedValueRegex;
        this.protectMode = protectMode == null ? MaskProtectMode.REJECT : protectMode;
        this.protectParamBindings = normalizeBindings(field, protectParamClassPath, protectParamFieldName,
                protectRecordIdField, protectVersionField, protectParamBindings);
    }

    public Field getField() {
        return field;
    }

    public MaskStrategy getStrategy() {
        return strategy;
    }

    public String getRegex() {
        return regex;
    }

    public String getReplacement() {
        return replacement;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public boolean isKeepEmpty() {
        return keepEmpty;
    }

    public boolean isHideWhenMasked() {
        return hideWhenMasked;
    }

    public boolean isValueProtectEnabled() {
        return valueProtectEnabled;
    }

    public String getProtectParamClassPath() {
        return protectParamClassPath;
    }

    public String getProtectParamFieldName() {
        return protectParamFieldName;
    }

    public List<MaskProtectBinding> getProtectParamBindings() {
        return protectParamBindings;
    }

    public String getProtectRecordIdField() {
        return protectRecordIdField;
    }

    public String getProtectVersionField() {
        return protectVersionField;
    }

    public MaskProtectVerifyMode getProtectVerifyMode() {
        return protectVerifyMode;
    }

    public String getProtectTableName() {
        return protectTableName;
    }

    public String getProtectRecordIdColumn() {
        return protectRecordIdColumn;
    }

    public String getProtectValueColumn() {
        return protectValueColumn;
    }

    public String getProtectDeletedColumn() {
        return protectDeletedColumn;
    }

    public Integer getProtectTtlSeconds() {
        return protectTtlSeconds;
    }

    public String getProtectMaskedValueRegex() {
        return protectMaskedValueRegex;
    }

    public MaskProtectMode getProtectMode() {
        return protectMode;
    }

    public boolean isContainer() {
        return container;
    }

    public int getSort() {
        return sort;
    }

    /**
     * 是否需要直接对该字段做脱敏处理（有显式策略）。
     * container 字段（无策略，仅用于递归）不会执行 handler。
     */
    public boolean hasStrategy() {
        return strategy != null && strategy != MaskStrategy.AUTO_FROM_CONFIG;
    }

    private List<MaskProtectBinding> normalizeBindings(Field field, String paramClassPath, String paramFieldName,
                                                       String recordIdField, String versionField,
                                                       List<MaskProtectBinding> bindings) {
        List<MaskProtectBinding> result = new ArrayList<>();
        if (bindings != null) {
            for (MaskProtectBinding binding : bindings) {
                MaskProtectBinding normalized = normalizeBinding(binding, field, recordIdField, versionField);
                if (normalized != null) {
                    result.add(normalized);
                }
            }
        }
        if (result.isEmpty() && paramClassPath != null && !paramClassPath.isEmpty()) {
            MaskProtectBinding binding = new MaskProtectBinding();
            binding.setParamClassPath(paramClassPath);
            binding.setParamFieldName(paramFieldName == null || paramFieldName.isEmpty()
                    ? field.getName() : paramFieldName);
            binding.setParamRecordIdField(recordIdField);
            binding.setParamVersionField(versionField);
            MaskProtectBinding normalized = normalizeBinding(binding, field, recordIdField, versionField);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private MaskProtectBinding normalizeBinding(MaskProtectBinding source, Field field,
                                                String recordIdField, String versionField) {
        if (source == null || source.getParamClassPath() == null || source.getParamClassPath().isEmpty()) {
            return null;
        }
        MaskProtectBinding target = new MaskProtectBinding();
        target.setParamClassPath(source.getParamClassPath());
        target.setParamFieldName(source.getParamFieldName() == null || source.getParamFieldName().isEmpty()
                ? field.getName() : source.getParamFieldName());
        target.setParamRecordIdField(source.getParamRecordIdField() == null || source.getParamRecordIdField().isEmpty()
                ? recordIdField : source.getParamRecordIdField());
        target.setParamVersionField(source.getParamVersionField() == null || source.getParamVersionField().isEmpty()
                ? versionField : source.getParamVersionField());
        if (target.getParamRecordIdField() == null || target.getParamRecordIdField().isEmpty()) {
            return null;
        }
        return target;
    }

    private static List<MaskProtectBinding> singletonBinding(String paramClassPath, String paramFieldName,
                                                             String recordIdField, String versionField) {
        if (paramClassPath == null || paramClassPath.isEmpty()) {
            return Collections.emptyList();
        }
        MaskProtectBinding binding = new MaskProtectBinding();
        binding.setParamClassPath(paramClassPath);
        binding.setParamFieldName(paramFieldName);
        binding.setParamRecordIdField(recordIdField);
        binding.setParamVersionField(versionField);
        return Collections.singletonList(binding);
    }
}
