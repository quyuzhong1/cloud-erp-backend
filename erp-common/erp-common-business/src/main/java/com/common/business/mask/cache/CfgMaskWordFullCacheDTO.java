package com.common.business.mask.cache;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 脱敏词典 Redis 全量载荷
 *
 * <p>同时作为：Redis Bucket 缓存值 / Redis miss 时 Feign 回源返回类型。
 * 与 {@link CfgMaskFieldFullCacheDTO} 一致携带 {@code version}，业务节点据此同步 SensitiveWordBs 引擎。</p>
 *
 * @author cloud-erp
 */
@Data
public class CfgMaskWordFullCacheDTO {

    /**
     * 全量数据版本号（{@link System#currentTimeMillis()}）
     */
    private long version;

    /**
     * 全量词典条目（未禁用且未删除）
     */
    private List<CfgMaskWordSnapshotEntry> data = new ArrayList<>();
}
