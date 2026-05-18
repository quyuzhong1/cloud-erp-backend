package com.common.business.mask.cache;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段脱敏配置 Redis 全量载荷
 *
 * <p>同时作为：</p>
 * <ol>
 *   <li>Redis Bucket 中的全量缓存值</li>
 *   <li>业务节点 Redis miss 时通过 Feign 拉取 sys {@code listAll} 的返回类型</li>
 * </ol>
 *
 * <p>携带 {@code version}，业务节点据此判断类元数据缓存是否需要失效。</p>
 *
 * @author cloud-erp
 */
@Data
public class CfgMaskFieldFullCacheDTO {

    /**
     * 全量数据版本号（sys 端生成瞬间的 {@link System#currentTimeMillis()}）
     */
    private long version;

    /**
     * 全量配置条目（未禁用且未删除）
     */
    private List<CfgMaskFieldSnapshotEntry> data = new ArrayList<>();
}
