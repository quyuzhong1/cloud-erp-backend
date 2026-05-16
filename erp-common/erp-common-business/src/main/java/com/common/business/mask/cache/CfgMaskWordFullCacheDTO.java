package com.common.business.mask.cache;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 词典全量推送载荷
 *
 * <p>同时作为：sys 广播载荷 / Redis Bucket 持久化载荷 / Feign 兜底返回类型，
 * 与 {@link CfgMaskFieldFullCacheDTO} 一致携带 {@code version}，订阅端按版本号单调递增校验丢弃乱序消息。</p>
 *
 * @author cloud-erp
 */
@Data
public class CfgMaskWordFullCacheDTO {

    /**
     * 全量数据版本号（{@link System#currentTimeMillis()}），订阅端忽略乱序 / 旧版本消息
     */
    private long version;

    /**
     * 全量词典条目（仅启用且未删除）
     */
    private List<CfgMaskWordSnapshotEntry> data = new ArrayList<>();
}
