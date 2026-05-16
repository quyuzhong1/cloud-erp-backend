package com.common.business.mask.cache;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 全量配置推送载荷
 *
 * <p>同时作为：</p>
 * <ol>
 *   <li>sys 服务通过 Redis Pub/Sub 广播给各业务节点的载荷</li>
 *   <li>业务节点冷启动 / 兜底从 Redis Bucket 读取的全量数据</li>
 *   <li>业务节点冷启动通过 Feign 拉取 sys {@code listAll} 的返回类型</li>
 * </ol>
 *
 * <p>与 {@link com.common.business.dto.DorisQuerySettingFullCacheDTO} 一致：
 * 携带 {@code version}，订阅端按版本号单调递增校验丢弃乱序消息。</p>
 *
 * @author cloud-erp
 */
@Data
public class CfgMaskFieldFullCacheDTO {

    /**
     * 全量数据版本号（sys 端发布瞬间的 {@link System#currentTimeMillis()}），
     * 订阅端通过该字段忽略乱序 / 旧版本消息
     */
    private long version;

    /**
     * 全量配置条目（仅启用且未删除）
     */
    private List<CfgMaskFieldSnapshotEntry> data = new ArrayList<>();
}
