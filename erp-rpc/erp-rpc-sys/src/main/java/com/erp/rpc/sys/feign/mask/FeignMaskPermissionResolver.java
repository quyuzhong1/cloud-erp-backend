package com.erp.rpc.sys.feign.mask;

import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.mask.MaskPermissionResolver;
import com.common.business.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于 {@link SysUserFeign} 的权限解析实现：现查 + 60s 本地 TTL + Redis Pub/Sub 主动失效
 *
 * <h3>为什么必须 Feign 现查</h3>
 * <p>项目网关用 {@link LoginUser#simpleLoginUser(LoginUser)} 把用户信息压缩进 HTTP Header
 * 转发到下游服务，<b>故意丢弃了 {@code permissionList}</b> 以避免撑爆 Header。
 * 业务服务下游 {@code user.getPermissionList()} 永远是 {@code null}，
 * 必须靠 {@code SysUserFeign.getRequestPermissionsList(uid)} 现查。</p>
 *
 * <h3>缓存设计</h3>
 * <table>
 *   <tr><th>维度</th><th>策略</th></tr>
 *   <tr><td>缓存粒度</td><td>uid → Set&lt;String&gt;</td></tr>
 *   <tr><td>缓存位置</td><td>本地 {@link ConcurrentHashMap}（每 Pod 独立）</td></tr>
 *   <tr><td>TTL</td><td>60s 兜底；超过 TTL 自动重新拉</td></tr>
 *   <tr><td>主动失效</td><td>订阅 {@link com.common.business.constant.RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}，
 *     由 sys 在权限写入路径广播；详见 {@code MaskPermissionEvictListener}</td></tr>
 *   <tr><td>缓存上限</td><td>{@link #MAX_CACHE_SIZE}，超限整体清空（极简防膨胀，权限场景活跃用户数有限）</td></tr>
 *   <tr><td>Feign 失败降级</td><td>若有历史缓存值则返回旧值；否则返回空集合（保守：脱敏）</td></tr>
 * </table>
 *
 * <h3>性能账</h3>
 * <pre>
 * 假设：500 QPS / Pod，活跃 200 用户，60s TTL
 * 命中率 ≈ 1 - (200 / (500 * 60)) = 99.3%
 * 实际 Feign 调用 = 200 次 / 60s = 3.3 次/秒/Pod
 * 全集群 10 Pod = 33 次/秒 sys Feign 调用，可接受
 * </pre>
 *
 * <h3>线程安全</h3>
 * <p>{@link ConcurrentHashMap} + 不可变 {@code Set} 值即可，无需额外锁。
 * 同一 uid 在 TTL 失效瞬间多线程并发调用，会发起多次 Feign（罕见、可接受）。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
@Primary
public class FeignMaskPermissionResolver implements MaskPermissionResolver {

    /**
     * 缓存条目存活时长（毫秒），与 Redis Pub/Sub 主动失效互为兜底
     */
    static final long TTL_MS = 60_000L;

    /**
     * 单 Pod 缓存条目上限。超过则整体清空，避免大用户量 + 长 TTL 内存膨胀。
     * 1 万用户 × 平均 50 权限码 × 30 字节 ≈ 15MB 内存，安全。
     */
    static final int MAX_CACHE_SIZE = 10_000;

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>(256);

    @Autowired
    private SysUserFeign sysUserFeign;

    @Override
    public Set<String> resolve(LoginUser user) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return Collections.emptySet();
        }
        String uid = user.getUid();
        long now = System.currentTimeMillis();
        Entry hit = cache.get(uid);
        if (hit != null && now - hit.ts < TTL_MS) {
            return hit.perms;
        }

        try {
            List<UserRequestPermissionsDTO> list = sysUserFeign.getRequestPermissionsList(uid);
            Set<String> perms = list == null
                    ? Collections.emptySet()
                    : Collections.unmodifiableSet(list.stream()
                        .filter(Objects::nonNull)
                        .map(UserRequestPermissionsDTO::getPermissionsCode)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toCollection(HashSet::new)));

            if (cache.size() > MAX_CACHE_SIZE) {
                log.warn("FeignMaskPermissionResolver cache size={} exceeded {}, clear all",
                        cache.size(), MAX_CACHE_SIZE);
                cache.clear();
            }
            cache.put(uid, new Entry(perms, now));
            return perms;
        } catch (Exception e) {
            if (hit != null) {
                log.warn("FeignMaskPermissionResolver feign failed, fallback to stale cache uid={}, "
                        + "staleAgeMs={}, msg={}", uid, now - hit.ts, e.getMessage());
                return hit.perms;
            }
            log.warn("FeignMaskPermissionResolver feign failed and no stale cache uid={}, msg={}",
                    uid, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 失效指定 uid 集合的缓存（由 {@code MaskPermissionEvictListener} 收到 Pub/Sub 后调用）
     */
    public void evict(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        for (String uid : uids) {
            if (uid != null) {
                cache.remove(uid);
            }
        }
    }

    /**
     * 失效全部缓存（由 {@code MaskPermissionEvictListener} 收到 Pub/Sub ALL 类型时调用）
     */
    public void evictAll() {
        cache.clear();
    }

    /**
     * 缓存运维视图（供诊断接口使用）
     */
    public Map<String, Object> snapshotView() {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("size", cache.size());
        view.put("ttlMs", TTL_MS);
        view.put("maxCacheSize", MAX_CACHE_SIZE);
        Map<String, Object> entries = new java.util.LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Entry> e : cache.entrySet()) {
            Map<String, Object> view1 = new java.util.LinkedHashMap<>();
            view1.put("permsCount", e.getValue().perms.size());
            view1.put("ageMs", now - e.getValue().ts);
            view1.put("expiredByTtl", now - e.getValue().ts >= TTL_MS);
            entries.put(e.getKey(), view1);
        }
        view.put("entries", entries);
        return view;
    }

    private static final class Entry {
        final Set<String> perms;
        final long ts;

        Entry(Set<String> perms, long ts) {
            this.perms = perms;
            this.ts = ts;
        }
    }
}
