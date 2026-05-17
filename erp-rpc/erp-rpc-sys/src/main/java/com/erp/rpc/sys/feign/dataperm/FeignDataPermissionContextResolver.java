package com.erp.rpc.sys.feign.dataperm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.erp.model.sys.dto.DataPermissionContextDTO;
import com.erp.rpc.sys.feign.DataPermissionFeign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限上下文解析器：Feign 现查 + 本地 5 分钟 TTL 缓存
 *
 * <h3>背景</h3>
 * <p>历史 {@code DataPermissionAspect} 每个 {@code @DataPermission} 注解的接口都打 5 次 sys Feign
 * （getRequestPermissionsList / getRoleIdList / getDepUserList / getShopUserList / getWarehouseUserList），
 * 500 QPS × 10 Pod = 2.5 万次/秒的 Feign，sys 撑不住。</p>
 *
 * <h3>本类设计</h3>
 * <ul>
 *   <li>替换为 1 次聚合 Feign（{@link DataPermissionFeign#getDataPermissionContext}）</li>
 *   <li>本地 Caffeine cache：uid → {@link DataPermissionContextDTO}</li>
 *   <li>TTL {@link #TTL_MS}（5 分钟），过期重新拉</li>
 *   <li>缓存条目上限 {@link #MAX_CACHE_SIZE}，由 Caffeine 按访问热度淘汰，避免手写 clear-all 抖动</li>
 *   <li><b>Feign 失败降级</b>：未命中/过期后加载失败返回 {@code empty()}，不把失败结果写入 cache
 *       —— 保守策略，业务上看到"没权限"而不是 500</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>Caffeine cache 线程安全；{@link DataPermissionContextDTO} 内部 List 字段在 sys 侧组装后视为不可变。
 * 同 uid 在 TTL 失效瞬间通过 {@code cache.get(key, mappingFunction)} 合并加载，避免并发 miss 重复打 sys。</p>
 *
 * <h3>失效</h3>
 * <ul>
 *   <li><b>主动失效（秒级）</b>：sys 服务在 6 张表（sys_role_menu / sys_role_user /
 *       sys_department_user / sys_user_shop / sys_user_warehouse / sys_user）写入路径
 *       publish 失效广播；业务节点 {@code DataPermissionContextEvictListener} 订阅后调
 *       {@link #evict} / {@link #evictAll}。</li>
 *   <li><b>TTL 兜底（5 分钟）</b>：失效消息丢失（Redis 抖动 / listener 启动时漏订阅）也会自动收敛。</li>
 *   <li><b>诊断手工触发</b>：{@code POST /dataPermCache/evict?uids=u1,u2 } 或 {@code ?all=true }。</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class FeignDataPermissionContextResolver {

    /**
     * 缓存条目存活时长（毫秒）。当前为 5 分钟，与 mask 权限 cache 一致，便于联调和心智统一。
     */
    static final long TTL_MS = 60_000L*5;

    /**
     * 单 Pod 缓存条目上限。
     *
     * <p>按线上 400 人规模 + 单节点 2GB JVM 测算：</p>
     * <ul>
     *   <li>普通用户单条 ≈ 54KB（permissions 43KB + depUser 4KB + shop 4.4KB + warehouse 2.2KB + ArrayList 头部）</li>
     *   <li>含 5% 超管/根部门账号，平均 ~80KB/条；满载 400 人约 22-30MB，占 2GB 的 1-1.5%</li>
     *   <li>上限设 1000：留 2.5 倍冗余应对未来用户增长 / 测试账号 / 异常缓存沉积</li>
     *   <li>1000 × 平均 100KB worst case ≈ 100MB，仍只占 2GB 的 5%</li>
     * </ul>
     *
     * <p>未来用户规模显著扩大（如 &gt; 2000 人）时再调大；当前 1000 足够且能起到真正的"超限保护"作用。</p>
     */
    static final int MAX_CACHE_SIZE = 1_000;

    /**
     * 超大 depUserList 警戒线。
     *
     * <p>挂在根部门下的特殊账号（总裁 / 财务总监 / 系统超管）depUserList 会等于"该根部门下所有用户 id"。
     * 线上 400 人规模下，正常账号 depUserList ≤ 100；超过 500 几乎可以断定是"根部门超管账号"，
     * 单条体积会膨胀到 100KB+，命中即打 warn 便于运维识别。</p>
     *
     * <p>注意：仍然会缓存（不缓存反而让该用户每次都重打 Feign，对 sys 更不利）。
     * 警戒线只用于打 warn 日志和后续监控；如果监控显示同一 uid 频繁触发，
     * 再考虑"该 uid 单独不缓存 / 缩短 TTL"等差异化策略。</p>
     */
    static final int DEP_USER_LARGE_WARN = 500;

    private final Cache<String, DataPermissionContextDTO> cache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_MS, TimeUnit.MILLISECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .recordStats()
            .build();

    @Autowired
    private DataPermissionFeign dataPermissionFeign;

    /**
     * 拿当前用户的数据权限上下文。
     *
     * <ul>
     *   <li>命中且未过期 → 直接返回缓存（< 1μs）</li>
     *   <li>未命中 / 过期 → 调聚合 Feign 一次，写 cache</li>
     *   <li>Feign 异常 → 返回 {@link DataPermissionContextDTO#empty()}，且不缓存失败结果</li>
     * </ul>
     *
     * <p>返回值<b>保证非 null</b>，业务方无需判空。</p>
     */
    public DataPermissionContextDTO resolve(String userId) {
        if (userId == null || userId.isEmpty()) {
            return DataPermissionContextDTO.empty();
        }
        try {
            return cache.get(userId, this::loadContext);
        } catch (Exception e) {
            log.warn("FeignDataPermissionContextResolver feign failed and no cached value uid={}, msg={}",
                    userId, e.getMessage());
            return DataPermissionContextDTO.empty();
        }
    }

    private DataPermissionContextDTO loadContext(String userId) {
        DataPermissionContextDTO ctx = dataPermissionFeign.getDataPermissionContext(userId);
        if (ctx == null) {
            ctx = DataPermissionContextDTO.empty();
        }

        int depSize = ctx.getDepUserList() == null ? 0 : ctx.getDepUserList().size();
        if (depSize >= DEP_USER_LARGE_WARN) {
            log.warn("FeignDataPermissionContextResolver large depUserList cached, uid={}, depSize={}, "
                    + "cacheSize={}", userId, depSize, cache.estimatedSize());
        }
        return ctx;
    }

    /**
     * 失效指定 uid 集合：由 {@link DataPermissionContextEvictListener} 收到 USER 类型广播后调用，
     * 也供 {@code /dataPermCache/evict} 诊断接口手动触发。
     */
    public void evict(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String uid : userIds) {
            if (uid != null) {
                cache.invalidate(uid);
            }
        }
    }

    /**
     * 失效全部缓存：由 {@link DataPermissionContextEvictListener} 收到 ALL 类型广播后调用
     * （角色/菜单大改场景），也供 {@code /dataPermCache/evict?all=true} 诊断接口手动触发。
     */
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
        view.put("depUserLargeWarn", DEP_USER_LARGE_WARN);
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
        Optional<Policy.Expiration<String, DataPermissionContextDTO>> expiration = cache.policy().expireAfterWrite();
        for (Map.Entry<String, DataPermissionContextDTO> e : cache.asMap().entrySet()) {
            DataPermissionContextDTO ctx = e.getValue();
            Map<String, Object> item = new LinkedHashMap<>();
            OptionalLong ageMs = expiration.isPresent()
                    ? expiration.get().ageOf(e.getKey(), TimeUnit.MILLISECONDS)
                    : OptionalLong.empty();
            item.put("ageMs", ageMs.isPresent() ? ageMs.getAsLong() : null);
            item.put("expiredByTtl", ageMs.isPresent() && ageMs.getAsLong() >= TTL_MS);
            item.put("permSize", ctx.getPermissionsList() == null ? 0 : ctx.getPermissionsList().size());
            item.put("roleIdSize", ctx.getRoleIdList() == null ? 0 : ctx.getRoleIdList().size());
            item.put("depUserSize", ctx.getDepUserList() == null ? 0 : ctx.getDepUserList().size());
            item.put("shopSize", ctx.getShopUserList() == null ? 0 : ctx.getShopUserList().size());
            item.put("warehouseSize", ctx.getWarehouseUserList() == null ? 0 : ctx.getWarehouseUserList().size());
            entries.put(e.getKey(), item);
        }
        view.put("entries", entries);
        return view;
    }
}
