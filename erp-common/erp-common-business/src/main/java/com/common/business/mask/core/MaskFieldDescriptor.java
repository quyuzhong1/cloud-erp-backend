package com.common.business.mask.core;

import com.common.business.mask.MaskStrategy;

import java.lang.reflect.Field;

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
}
