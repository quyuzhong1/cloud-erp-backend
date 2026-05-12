package com.common.business.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.dto.DorisQuerySettingFullCacheDTO;
import com.common.business.wrapper.FeignQuery;

import lombok.extern.slf4j.Slf4j;

/**
 * 动态数据源 Doris 路由配置全量本地缓存
 *
 * <p>
 * 配合 DMP 端 Redis Pub/Sub 广播实现："启动 Feign 拉一次全量 + Pub/Sub 实时刷新 + 5 分钟兜底回拉"，
 * 业务请求 ({@link com.common.business.filter.DynamicDataSourceFilter}) 直接走本进程内存快照，O(1) 命中、零 RPC。
 * </p>
 *
 * <p>容错策略：</p>
 * <ul>
 *   <li>启动同步采用异步首次执行（不阻塞 Spring 启动）；DMP 不可达时 snapshot 暂为空，filter 会按 null 走原始 chain，与原行为兼容。</li>
 *   <li>{@link #apply(DorisQuerySettingFullCacheDTO)} 通过 {@code version} 严格单调递增，忽略乱序/旧消息。</li>
 *   <li>定时器每 5 分钟兜底拉取一次全量，防止订阅断连或消息丢失。</li>
 * </ul>
 *
 * 仅当 {@code spring.datasource.dynamic.enabled=true} 时启用。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingLocalCache {

    /**
     * DMP 端被远程调用的类名与方法名（与 DMP {@code DmpHandlerCache#getAllDorisQuerySettings} 必须严格一致）
     */
    private static final String REMOTE_CLASS = "com.erp.server.dmp.inout.utils.DmpHandlerCache";
    private static final String REMOTE_METHOD = "getAllDorisQuerySettings";

    private static final long FALLBACK_SYNC_INTERVAL_MINUTES = 5L;

    /**
     * 当前生效的全量快照（不可变 Map + 版本号 + 最近一次同步时间）。
     * 读路径无锁：volatile 读 + Snapshot 内字段 final，安全发布。
     * 写路径 synchronized：apply 频率极低（≤1 次/秒），无需 CAS 自旋。
     */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    private ScheduledExecutorService scheduler;

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
     * 应用一次远程推送 / 拉取的全量快照；version 单调递增校验，忽略乱序/旧消息
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

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "doris-query-cfg-local-cache");
            t.setDaemon(true);
            return t;
        });
        // 首次延迟少量秒，等待 Spring 容器以及 Feign 客户端就绪后再尝试拉取
        scheduler.scheduleAtFixedRate(this::syncFromRemoteSilently,
                10L, FALLBACK_SYNC_INTERVAL_MINUTES * 60L, TimeUnit.SECONDS);
        log.info("DorisQuerySettingLocalCache initialized, fallback sync every {} min", FALLBACK_SYNC_INTERVAL_MINUTES);
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void syncFromRemoteSilently() {
        try {
            DorisQuerySettingFullCacheDTO remote = FeignQuery.invoke(
                    DorisQuerySettingFullCacheDTO.class, REMOTE_CLASS, REMOTE_METHOD);
            if (remote == null) {
                log.warn("DorisQuerySettingLocalCache sync remote returned null");
                return;
            }
            apply(remote);
        } catch (Throwable e) {
            log.warn("DorisQuerySettingLocalCache sync remote failed, current snapshot keeps unchanged", e);
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
