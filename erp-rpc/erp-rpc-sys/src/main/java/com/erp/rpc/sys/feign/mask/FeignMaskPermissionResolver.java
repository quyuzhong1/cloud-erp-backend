package com.erp.rpc.sys.feign.mask;

import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.mask.MaskPermissionResolver;
import com.common.business.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于 {@link SysUserFeign} 的权限解析实现：现查 + 本地 5 分钟 TTL 缓存 + Redis Pub/Sub 主动失效
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
 *   <tr><td>缓存位置</td><td>本地 Caffeine cache（每 Pod 独立）</td></tr>
 *   <tr><td>TTL</td><td>5 分钟兜底；超过 TTL 自动重新拉</td></tr>
 *   <tr><td>主动失效</td><td>订阅 {@link com.common.business.constant.RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}，
 *     由 sys 在权限写入路径广播；详见 {@code MaskPermissionEvictListener}</td></tr>
 *   <tr><td>缓存上限</td><td>{@link #MAX_CACHE_SIZE}，由 Caffeine 按访问热度淘汰冷门 entry</td></tr>
 *   <tr><td>Feign 失败降级</td><td>未命中/过期后加载失败返回空集合，且不缓存失败结果（保守：脱敏）</td></tr>
 * </table>
 *
 * <h3>性能账</h3>
 * <pre>
 * 假设：500 QPS / Pod，活跃 200 用户，5 分钟 TTL
 * 实际 Feign 调用 = 200 次 / 300s = 0.67 次/秒/Pod
 * 全集群 10 Pod = 6.7 次/秒 sys Feign 调用，可接受
 * </pre>
 *
 * <h3>线程安全</h3>
 * <p>Caffeine cache 线程安全，缓存值为不可变 {@code Set}。
 * 同 uid 在 TTL 失效瞬间通过 {@code cache.get(key, mappingFunction)} 合并加载，避免并发 miss 重复打 sys。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
@Primary
public class FeignMaskPermissionResolver implements MaskPermissionResolver {

    /**
     * 缓存条目存活时长（毫秒），与 Redis Pub/Sub 主动失效互为兜底。
     */
    static final long TTL_MS = 60_000L*5;

    /**
     * 单 Pod 缓存条目上限。超过后由 Caffeine 淘汰冷门 entry，避免大用户量 + 长 TTL 内存膨胀。
     * 1 万用户 × 平均 50 权限码 × 30 字节 ≈ 15MB 内存，安全。
     */
    static final int MAX_CACHE_SIZE = 10_000;

    private final Cache<String, Set<String>> cache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_MS, TimeUnit.MILLISECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .recordStats()
            .build();

    @Autowired
    private SysUserFeign sysUserFeign;

    @Override
    public Set<String> resolve(LoginUser user) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return Collections.emptySet();
        }
        String uid = user.getUid();
        try {
            return cache.get(uid, this::loadPermissions);
        } catch (Exception e) {
            log.warn("FeignMaskPermissionResolver feign failed and no cached value uid={}, msg={}", uid, e.getMessage());
            return Collections.emptySet();
        }
    }

    private Set<String> loadPermissions(String uid) {
        List<UserRequestPermissionsDTO> list = sysUserFeign.getRequestPermissionsList(uid);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> raw = list.stream()
                .filter(Objects::nonNull)
                .map(UserRequestPermissionsDTO::getPermissionsCode)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
        return Collections.unmodifiableSet(raw);
    }

    /**
     * 失效指定 uid 集合的缓存（由 {@code MaskPermissionEvictListener} 收到 Pub/Sub 后调用）
     */
    @Override
    public void evict(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        for (String uid : uids) {
            if (uid != null) {
                cache.invalidate(uid);
            }
        }
    }

    /**
     * 失效全部缓存（由 {@code MaskPermissionEvictListener} 收到 Pub/Sub ALL 类型时调用）
     */
    @Override
    public void evictAll() {
        cache.invalidateAll();
    }

    /**
     * 缓存运维视图（供诊断接口使用）
     */
    public Map<String, Object> snapshotView() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("size", cache.estimatedSize());
        view.put("ttlMs", TTL_MS);
        view.put("maxCacheSize", MAX_CACHE_SIZE);
        CacheStats stats = cache.stats();
        Map<String, Object> statsView = new LinkedHashMap<>();
        statsView.put("requestCount", stats.requestCount());
        statsView.put("hitCount", stats.hitCount());
        statsView.put("missCount", stats.missCount());
        statsView.put("hitRate", stats.hitRate());
        statsView.put("evictionCount", stats.evictionCount());
        statsView.put("loadSuccessCount", stats.loadSuccessCount());
        statsView.put("loadFailureCount", stats.loadFailureCount());
        view.put("stats", statsView);

        Map<String, Object> entries = new LinkedHashMap<>();
        Optional<Policy.Expiration<String, Set<String>>> expiration = cache.policy().expireAfterWrite();
        for (Map.Entry<String, Set<String>> e : cache.asMap().entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            OptionalLong ageMs = expiration.isPresent()
                    ? expiration.get().ageOf(e.getKey(), TimeUnit.MILLISECONDS)
                    : OptionalLong.empty();
            item.put("permsCount", e.getValue().size());
            item.put("ageMs", ageMs.isPresent() ? ageMs.getAsLong() : null);
            item.put("expiredByTtl", ageMs.isPresent() && ageMs.getAsLong() >= TTL_MS);
            entries.put(e.getKey(), item);
        }
        view.put("entries", entries);
        return view;
    }
}
