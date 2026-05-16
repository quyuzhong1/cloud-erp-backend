package com.common.business.mask.cache;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.core.MaskClassDescriptorRegistry;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置本地全量快照
 *
 * <p>架构与 {@link com.common.business.cache.DorisQuerySettingLocalCache} 完全一致：</p>
 * <ul>
 *   <li>启动时从 Redis Bucket {@link RedisCacheConstants#MASK_FIELD_CFG_FULL_KEY} 同步一次冷启动数据</li>
 *   <li>运行期订阅 {@link RedisCacheConstants#MASK_FIELD_CFG_REFRESH_CHANNEL}（详见
 *       {@link com.common.business.mask.listener.CfgMaskFieldRefreshListener}）</li>
 *   <li>{@link #apply(CfgMaskFieldFullCacheDTO)} 通过 {@code version} 单调递增校验自动忽略乱序消息</li>
 *   <li>每次成功 apply 后调用 {@link MaskClassDescriptorRegistry#clear()}，
 *       让下次访问的类元数据重新合并新配置（哨兵 NO_MASK 也会被清空）</li>
 * </ul>
 *
 * <p>极端情况下（Redis 没数据 + sys 暂未启动）snapshot 暂为空，不影响业务运行：
 * 此时仅注解生效。</p>
 *
 * <p>本类不绑定 {@code spring.datasource.dynamic.enabled}，是无条件 Bean
 * （脱敏框架对所有业务服务必须可用）。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskFieldLocalCache {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 类元数据注册中心：每次配置 apply 后整体清空，下次扫描时重新合并新配置
     */
    @Autowired(required = false)
    private MaskClassDescriptorRegistry descriptorRegistry;

    /**
     * 当前生效的全量快照（不可变 Map + 版本号 + 最近一次同步时间）。
     * 读路径无锁：volatile 读 + Snapshot 内字段 final，安全发布。
     * 写路径 synchronized：apply 频率极低（≤1 次/秒），无需 CAS 自旋。
     */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    /**
     * 按 (classPath, fieldName) 查询配置，O(1)；未命中返回 null
     */
    public CfgMaskFieldSnapshotEntry get(String classPath, String fieldName) {
        if (classPath == null || fieldName == null) {
            return null;
        }
        return snapshot.data.get(classPath + "#" + fieldName);
    }

    /**
     * 应用一次远程推送 / 持久化的全量快照
     *
     * <p>步骤：</p>
     * <ol>
     *   <li>version 单调递增校验，旧消息直接忽略；</li>
     *   <li>新数据 build 成不可变 map 切到 {@code snapshot}；</li>
     *   <li><b>增量计算受影响的 classPath 集合</b>，只 evict 命中的类元数据，
     *       <b>不再整体 clear</b>，避免业务高峰期一次配置改动导致全集群反射风暴。</li>
     * </ol>
     */
    public synchronized void apply(CfgMaskFieldFullCacheDTO payload) {
        if (payload == null || payload.getVersion() <= snapshot.version) {
            return;
        }
        List<CfgMaskFieldSnapshotEntry> data = payload.getData();
        Map<String, CfgMaskFieldSnapshotEntry> next;
        if (data == null || data.isEmpty()) {
            next = Collections.emptyMap();
        } else {
            HashMap<String, CfgMaskFieldSnapshotEntry> tmp = new HashMap<>(data.size() * 2);
            for (CfgMaskFieldSnapshotEntry entry : data) {
                if (entry == null || entry.getClassPath() == null || entry.getFieldName() == null
                        || entry.getStrategy() == null) {
                    continue;
                }
                if (entry.getStrategy() == MaskStrategy.AUTO_FROM_CONFIG) {
                    continue;
                }
                tmp.put(entry.getClassPath() + "#" + entry.getFieldName(), entry);
            }
            next = Collections.unmodifiableMap(tmp);
        }
        Snapshot prev = this.snapshot;
        this.snapshot = new Snapshot(next, payload.getVersion(), System.currentTimeMillis());

        if (descriptorRegistry != null) {
            Set<String> affected = computeAffectedClasses(prev.data, next);
            if (!affected.isEmpty()) {
                descriptorRegistry. evictByClassNames(affected);
            }
            log.info("CfgMaskFieldLocalCache apply ok, size={}, version={}, evictClasses={}",
                    next.size(), payload.getVersion(), affected.size());
        } else {
            log.info("CfgMaskFieldLocalCache apply ok, size={}, version={}", next.size(), payload.getVersion());
        }
    }

    /**
     * 计算"prev → next"切换需要 evict 的 classPath 集合：
     * <ul>
     *   <li>prev 中已删除的 (classPath#fieldName) → 该 classPath 入集合</li>
     *   <li>next 中新增的 (classPath#fieldName) → 该 classPath 入集合</li>
     *   <li>prev / next 都有但 strategy/regex/replacement/permission/hideWhenMasked
     *       任一不同 → 该 classPath 入集合</li>
     * </ul>
     */
    private static Set<String> computeAffectedClasses(
            Map<String, CfgMaskFieldSnapshotEntry> prev,
            Map<String, CfgMaskFieldSnapshotEntry> next) {
        if ((prev == null || prev.isEmpty()) && next.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> affected = new HashSet<>();
        if (prev != null) {
            for (Map.Entry<String, CfgMaskFieldSnapshotEntry> e : prev.entrySet()) {
                CfgMaskFieldSnapshotEntry n = next.get(e.getKey());
                if (n == null || !sameForCache(e.getValue(), n)) {
                    affected.add(e.getValue().getClassPath());
                }
            }
        }
        for (Map.Entry<String, CfgMaskFieldSnapshotEntry> e : next.entrySet()) {
            if (prev == null || !prev.containsKey(e.getKey())) {
                affected.add(e.getValue().getClassPath());
            }
        }
        return affected;
    }

    /**
     * 仅比较影响 {@code MaskFieldDescriptor} 构造结果的字段。
     * 注意不要比 lastSyncMillis / version 这类与缓存语义无关的字段。
     */
    private static boolean sameForCache(CfgMaskFieldSnapshotEntry a, CfgMaskFieldSnapshotEntry b) {
        return a.getStrategy() == b.getStrategy()
                && a.isHideWhenMasked() == b.isHideWhenMasked()
                && Objects.equals(a.getRegex(), b.getRegex())
                && Objects.equals(a.getReplacement(), b.getReplacement())
                && Objects.equals(a.getPermission(), b.getPermission());
    }

    /**
     * 当前快照的运维视图（不可变 Map），含 version / size / lastSyncMillis / data
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
            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY);
            String body = bucket.get();
            if (body == null || body.isEmpty()) {
                log.info("CfgMaskFieldLocalCache init: redis bucket empty, wait for next broadcast");
                return;
            }
            CfgMaskFieldFullCacheDTO payload = JSON.parseObject(body, CfgMaskFieldFullCacheDTO.class);
            apply(payload);
        } catch (Throwable e) {
            log.warn("CfgMaskFieldLocalCache init load from redis bucket failed, wait for next broadcast", e);
        }
    }

    private static final class Snapshot {
        private static final Snapshot EMPTY = new Snapshot(Collections.emptyMap(), 0L, 0L);

        private final Map<String, CfgMaskFieldSnapshotEntry> data;
        private final long version;
        private final long lastSyncMillis;

        private Snapshot(Map<String, CfgMaskFieldSnapshotEntry> data, long version, long lastSyncMillis) {
            this.data = data;
            this.version = version;
            this.lastSyncMillis = lastSyncMillis;
        }
    }
}
