package com.common.business.mask.core;

import com.common.business.mask.MaskStrategy;
import com.common.business.mask.protect.MaskProtectBinding;
import com.common.business.mask.protect.MaskProtectMode;

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
     * 保存接口入参 DTO 绑定列表。
     */
    private final List<MaskProtectBinding> protectParamBindings;

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
                hideWhenMasked, container, sort, false, "", "", "", "",
                MaskProtectMode.REJECT, Collections.emptyList());
    }

    public MaskFieldDescriptor(Field field, MaskStrategy strategy, String regex, String replacement,
                               String permission, boolean recursive, boolean keepEmpty,
                               boolean hideWhenMasked, boolean container, int sort,
                               boolean valueProtectEnabled, String protectTableName,
                               String protectRecordIdColumn, String protectValueColumn,
                               String protectDeletedColumn, MaskProtectMode protectMode,
                               List<MaskProtectBinding> protectParamBindings) {
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
        this.protectTableName = protectTableName == null ? "" : protectTableName;
        this.protectRecordIdColumn = protectRecordIdColumn == null ? "" : protectRecordIdColumn;
        this.protectValueColumn = protectValueColumn == null ? "" : protectValueColumn;
        this.protectDeletedColumn = protectDeletedColumn == null ? "" : protectDeletedColumn;
        this.protectMode = protectMode == null ? MaskProtectMode.REJECT : protectMode;
        this.protectParamBindings = normalizeBindings(field, protectParamBindings);
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

    public List<MaskProtectBinding> getProtectParamBindings() {
        return protectParamBindings;
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

    private List<MaskProtectBinding> normalizeBindings(Field field, List<MaskProtectBinding> bindings) {
        List<MaskProtectBinding> result = new ArrayList<>();
        if (bindings != null) {
            for (MaskProtectBinding binding : bindings) {
                MaskProtectBinding normalized = normalizeBinding(binding, field);
                if (normalized != null) {
                    result.add(normalized);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private MaskProtectBinding normalizeBinding(MaskProtectBinding source, Field field) {
        if (source == null || source.getParamClassPath() == null || source.getParamClassPath().isEmpty()) {
            return null;
        }
        MaskProtectBinding target = new MaskProtectBinding();
        target.setParamClassPath(source.getParamClassPath());
        target.setParamFieldName(source.getParamFieldName() == null || source.getParamFieldName().isEmpty()
                ? field.getName() : source.getParamFieldName());
        target.setParamRecordIdField(source.getParamRecordIdField());
        if (target.getParamRecordIdField() == null || target.getParamRecordIdField().isEmpty()) {
            return null;
        }
        return target;
    }
}
