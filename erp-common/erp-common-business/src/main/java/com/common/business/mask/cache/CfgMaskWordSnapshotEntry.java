package com.common.business.mask.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置表 {@code cfg_mask_word} 单行的 Redis 快照视图
 *
 * <p>只携带引擎需要的最小字段：词类型 + 词内容；分类标签和备注属于运维信息，不参与运行期判定。</p>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgMaskWordSnapshotEntry {

    /** 词类型：0=黑名单（敏感词） */
    public static final int WORD_TYPE_DENY = 0;

    /** 词类型：1=白名单（豁免） */
    public static final int WORD_TYPE_ALLOW = 1;

    /** 词类型：0=黑名单（敏感词），1=白名单（豁免） */
    private Integer wordType;

    /** 词内容 */
    private String word;

    /** 生效顺序，越小越先灌入引擎 */
    private Integer sort;
}
