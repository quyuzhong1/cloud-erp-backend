package com.common.business.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * 动态数据源 Doris 路由配置全量快照
 * 同时作为：
 * 1. DMP 通过 Redis Pub/Sub 广播给各业务节点的载荷
 * 2. 业务节点冷启动 / 兜底 Feign 拉取 DMP {@code DmpHandlerCache#getAllDorisQuerySettings} 的返回类型
 *
 * @author plan: 动态数据源本地缓存 Redis 广播刷新方案
 */
@Data
public class DorisQuerySettingFullCacheDTO {

    /**
     * 全量数据版本号，使用 DMP 端发布瞬间的 System.currentTimeMillis()
     * 订阅端通过该字段忽略乱序/旧版本消息
     */
    private long version;

    /**
     * URI -> DorisQuerySettingDTO 全量映射，URI 已规范化为以 "/" 开头
     */
    private Map<String, DorisQuerySettingDTO> data = new HashMap<>();
}
