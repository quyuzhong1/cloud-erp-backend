package com.common.business.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.dto.DorisQuerySettingFullCacheDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 动态数据源 Doris 路由配置全量本地缓存
 *
 * <p>数据来源全部由 DMP 通过 Redis 推送 / 持久化：</p>
 * <ul>
 *   <li>启动时同步读取 Redis Bucket {@code DORIS_QUERY_CFG_FULL_KEY} 一次，避免冷启动空窗</li>
 *   <li>运行期订阅 {@code DORIS_QUERY_CFG_REFRESH_CHANNEL} 实时接收广播
 *       （详见 {@link com.common.business.listener.DorisQuerySettingRefreshListener}）</li>
 *   <li>DMP 端 5 分钟周期无条件广播作为兜底，覆盖订阅链路抖动</li>
 * </ul>
 *
 * <p>DMP 端写入严格"先写 Bucket 再 publish"，保证收到广播的节点 / 新启动节点
 * 从 Bucket 拿到的版本 ≥ 已广播版本，本端 {@link #apply(DorisQuerySettingFullCacheDTO)} 通过
 * {@code version} 单调递增校验自动忽略乱序 / 重复消息。</p>
 *
 * <p>极端情况（Redis 没数据 + DMP 不在线）下 snapshot 暂为空，
 * {@link com.common.business.filter.DynamicDataSourceFilter} 会按 null 走默认数据源（与原行为兼容）。</p>
 *
 * 仅当 {@code spring.datasource.dynamic.enabled=true} 时启用。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingLocalCache {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 当前生效的全量快照（不可变 Map + 版本号 + 最近一次同步时间）。
     * 读路径无锁：volatile 读 + Snapshot 内字段 final，安全发布。
     * 写路径 synchronized：apply 频率极低（≤1 次/秒），无需 CAS 自旋。
     */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    /**
     * 按 URI 查询路由配置，O(1)；未命中返回 null（与原 Feign 调用未命中行为一致）
     */
    public DorisQuerySettingDTO get(String requestURI) {
        if (requestURI == null) {
            return null;
        }
        String key = requestURI.startsWith("/") ? requestURI : "/" + requestURI;
        return snapshot.data.get(key);
    }

    /**
     * 应用一次远程推送 / 持久化的全量快照；version 单调递增校验，忽略乱序/旧消息
     */
    public synchronized void apply(DorisQuerySettingFullCacheDTO payload) {
        if (payload == null || payload.getVersion() <= snapshot.version) {
            return;
        }
        Map<String, DorisQuerySettingDTO> data = payload.getData();
        Map<String, DorisQuerySettingDTO> immutable = (data == null || data.isEmpty())
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(data));
        snapshot = new Snapshot(immutable, payload.getVersion(), System.currentTimeMillis());
        log.info("DorisQuerySettingLocalCache apply ok, size={}, version={}", immutable.size(), payload.getVersion());
    }

    /**
     * 当前快照的运维视图（不可变 Map），含 version / size / lastSyncMillis / data，
     * 供 {@link com.common.business.cache.controller.DorisQuerySettingCacheController} 暴露查询接口
     */
    public Map<String, Object> snapshotView() {
        Snapshot s = this.snapshot;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("version", s.version);
        view.put("size", s.data.size());
        view.put("lastSyncMillis", s.lastSyncMillis);
        view.put("data", s.data);
        return view;
    }

    @PostConstruct
    public void init() {
        try {
            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.DORIS_QUERY_CFG_FULL_KEY);
            String body = bucket.get();
            if (body == null || body.isEmpty()) {
                log.info("DorisQuerySettingLocalCache init: redis bucket empty, wait for next broadcast");
                return;
            }
            DorisQuerySettingFullCacheDTO payload = JSON.parseObject(body, DorisQuerySettingFullCacheDTO.class);
            apply(payload);
        } catch (Throwable e) {
            log.warn("DorisQuerySettingLocalCache init load from redis bucket failed, wait for next broadcast", e);
        }
    }

    private static final class Snapshot {
        private static final Snapshot EMPTY = new Snapshot(Collections.emptyMap(), 0L, 0L);

        private final Map<String, DorisQuerySettingDTO> data;
        private final long version;
        private final long lastSyncMillis;

        private Snapshot(Map<String, DorisQuerySettingDTO> data, long version, long lastSyncMillis) {
            this.data = data;
            this.version = version;
            this.lastSyncMillis = lastSyncMillis;
        }
    }
}
