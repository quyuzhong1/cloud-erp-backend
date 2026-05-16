package com.common.business.mask.core;

import java.util.Collections;
import java.util.List;

/**
 * 单个类的脱敏元数据快照
 *
 * <p>由 {@link MaskClassDescriptorRegistry#of(Class)} 懒加载构造并永久缓存。
 * 配置变更时统一调用 {@link MaskClassDescriptorRegistry#clear()} 整体失效，下次重建。</p>
 *
 * @author cloud-erp
 */
public final class MaskClassDescriptor {

    /**
     * 标记"该类无任何脱敏字段且不需要继续递归"的常量。
     * 命中时直接 O(1) 返回，避免重复反射扫描。
     */
    public static final MaskClassDescriptor NO_MASK = new MaskClassDescriptor(Collections.emptyList());

    private final List<MaskFieldDescriptor> fields;

    public MaskClassDescriptor(List<MaskFieldDescriptor> fields) {
        this.fields = fields == null ? Collections.emptyList() : Collections.unmodifiableList(fields);
    }

    public List<MaskFieldDescriptor> getFields() {
        return fields;
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
