package com.common.business.mask.cache;

import com.common.business.mask.MaskStrategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置表 {@code cfg_mask_field} 单行的本地快照视图
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

    /**
     * "不可见"语义开关：true=脱敏后再置 null（隐藏整字段），false=按 strategy 脱敏展示
     */
    private boolean hideWhenMasked;
}
