package com.common.business.mask.cache;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏词典 Redis cache-aside 访问器。
 *
 * <p>cfg_mask_word 的权威缓存放在 Redis。业务节点只保留一份 SensitiveWordBs 引擎镜像，
 * 每次版本变化时按差量同步到引擎；Redis miss 时通过 {@link CfgMaskWordCacheLoader}
 * 回源 sys 并回填 Redis。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskWordLocalCache {

    private static final long VERSION_CHECK_INTERVAL_MS = 1000L;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private ObjectProvider<SensitiveWordBs> sensitiveWordBsProvider;

    @Autowired
    private ObjectProvider<CfgMaskWordCacheLoader> loaderProvider;

    /** SensitiveWordBs 当前已同步的词典镜像，用于 diff 删除旧词。 */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    private volatile long lastCheckMillis;

    /**
     * AutoPiiMaskHandler 热路径调用：最多每秒检查一次 Redis 缓存版本。
     */
    public void refreshEngineIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
            return;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
                return;
            }
            lastCheckMillis = now;
            applyIfChanged(loadPayload());
        }
    }

    /**
     * 引擎完成构造后，把 Redis 中当前词典重放给引擎。
     */
    public synchronized void replay() {
        CfgMaskWordFullCacheDTO payload = loadPayload();
        if (payload != null && payload.getVersion() != snapshot.version) {
            applyIfChanged(payload);
            return;
        }
        replaySnapshotToEngine(snapshot);
    }

    /** 当前黑名单镜像（不可变）。 */
    public Set<String> denyWords() {
        return snapshot.deny;
    }

    /** 当前白名单镜像（不可变）。 */
    public Set<String> allowWords() {
        return snapshot.allow;
    }

    /**
     * 运维视图：展示 Redis 缓存和当前引擎镜像状态。
     */
    public Map<String, Object> snapshotView() {
        CfgMaskWordFullCacheDTO payload = loadPayload();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("cacheMode", "redis-cache-aside");
        view.put("redisKey", RedisCacheConstants.MASK_WORD_CFG_FULL_KEY);
        view.put("redisVersion", payload == null ? 0L : payload.getVersion());
        view.put("redisSize", payload == null || payload.getData() == null ? 0 : payload.getData().size());
        Snapshot s = this.snapshot;
        view.put("engineVersion", s.version);
        view.put("denySize", s.deny.size());
        view.put("allowSize", s.allow.size());
        view.put("lastSyncMillis", s.lastSyncMillis);
        view.put("denyWords", s.deny);
        view.put("allowWords", s.allow);
        return view;
    }

    @PostConstruct
    public void init() {
        try {
            replay();
        } catch (Throwable e) {
            log.warn("CfgMaskWord redis cache init failed, engine starts without custom dict", e);
        }
    }

    private void applyIfChanged(CfgMaskWordFullCacheDTO payload) {
        if (payload == null || payload.getVersion() == snapshot.version) {
            return;
        }
        Snapshot next = toSnapshot(payload);
        Snapshot prev = this.snapshot;
        this.snapshot = next;
        applyDiffToEngine(prev, next);
        log.info("CfgMaskWord redis cache synced to engine, denySize={}, allowSize={}, version={}",
                next.deny.size(), next.allow.size(), next.version);
    }

    private CfgMaskWordFullCacheDTO loadPayload() {
        RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_WORD_CFG_FULL_KEY);
        String body = null;
        try {
            body = bucket.get();
        } catch (Throwable e) {
            log.warn("CfgMaskWord redis get failed, fallback to loader, msg={}", e.getMessage());
        }
        if (body != null && !body.isEmpty()) {
            try {
                return JSON.parseObject(body, CfgMaskWordFullCacheDTO.class);
            } catch (Throwable e) {
                log.warn("CfgMaskWord redis payload parse failed, fallback to loader, msg={}", e.getMessage());
            }
        }
        CfgMaskWordFullCacheDTO loaded = loadFromSys();
        if (loaded != null) {
            try {
                bucket.set(JSON.toJSONString(loaded));
            } catch (Throwable e) {
                log.warn("CfgMaskWord redis set after loader failed, msg={}", e.getMessage());
            }
        }
        return loaded;
    }

    private CfgMaskWordFullCacheDTO loadFromSys() {
        CfgMaskWordCacheLoader loader = loaderProvider.getIfAvailable();
        if (loader == null) {
            return null;
        }
        CfgMaskWordFullCacheDTO loaded = loader.load();
        return loaded == null ? null : loaded;
    }

    private static Snapshot toSnapshot(CfgMaskWordFullCacheDTO payload) {
        Set<String> deny = new HashSet<>();
        Set<String> allow = new HashSet<>();
        List<CfgMaskWordSnapshotEntry> data = payload.getData();
        if (data != null && !data.isEmpty()) {
            List<CfgMaskWordSnapshotEntry> ordered = new ArrayList<>(data);
            ordered.sort(Comparator
                    .comparingInt((CfgMaskWordSnapshotEntry e) -> e.getSort() == null ? 0 : e.getSort())
                    .thenComparing(e -> e.getWord() == null ? "" : e.getWord()));
            for (CfgMaskWordSnapshotEntry e : ordered) {
                if (e == null || StringUtils.isBlank(e.getWord()) || e.getWordType() == null) {
                    continue;
                }
                if (e.getWordType() == CfgMaskWordSnapshotEntry.WORD_TYPE_DENY) {
                    deny.add(e.getWord());
                } else if (e.getWordType() == CfgMaskWordSnapshotEntry.WORD_TYPE_ALLOW) {
                    allow.add(e.getWord());
                }
            }
        }
        return new Snapshot(
                deny.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(deny),
                allow.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(allow),
                payload.getVersion(),
                System.currentTimeMillis());
    }

    private void applyDiffToEngine(Snapshot prev, Snapshot next) {
        SensitiveWordBs bs = sensitiveWordBsProvider.getIfAvailable();
        if (bs == null) {
            return;
        }
        try {
            List<String> denyToAdd = diff(next.deny, prev.deny);
            List<String> denyToRemove = diff(prev.deny, next.deny);
            if (!denyToAdd.isEmpty()) {
                bs.addWord(denyToAdd);
            }
            if (!denyToRemove.isEmpty()) {
                bs.removeWord(denyToRemove);
            }

            List<String> allowToAdd = diff(next.allow, prev.allow);
            List<String> allowToRemove = diff(prev.allow, next.allow);
            if (!allowToAdd.isEmpty()) {
                bs.addWordAllow(allowToAdd);
            }
            if (!allowToRemove.isEmpty()) {
                bs.removeWordAllow(allowToRemove);
            }
        } catch (Throwable e) {
            log.warn("CfgMaskWord sync diff to SensitiveWordBs failed", e);
        }
    }

    private void replaySnapshotToEngine(Snapshot s) {
        if (s == null || s == Snapshot.EMPTY) {
            return;
        }
        SensitiveWordBs bs = sensitiveWordBsProvider.getIfAvailable();
        if (bs == null) {
            return;
        }
        try {
            if (!s.deny.isEmpty()) {
                bs.addWord(new ArrayList<>(s.deny));
            }
            if (!s.allow.isEmpty()) {
                bs.addWordAllow(new ArrayList<>(s.allow));
            }
            log.info("CfgMaskWord replay to engine ok, denySize={}, allowSize={}, version={}",
                    s.deny.size(), s.allow.size(), s.version);
        } catch (Throwable e) {
            log.warn("CfgMaskWord replay to SensitiveWordBs failed", e);
        }
    }

    private static List<String> diff(Set<String> minuend, Set<String> subtrahend) {
        if (minuend == null || minuend.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String s : minuend) {
            if (subtrahend == null || !subtrahend.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }

    private static final class Snapshot {
        private static final Snapshot EMPTY = new Snapshot(
                Collections.emptySet(), Collections.emptySet(), 0L, 0L);

        private final Set<String> deny;
        private final Set<String> allow;
        private final long version;
        private final long lastSyncMillis;

        private Snapshot(Set<String> deny, Set<String> allow, long version, long lastSyncMillis) {
            this.deny = deny == null ? Collections.emptySet() : deny;
            this.allow = allow == null ? Collections.emptySet() : allow;
            this.version = version;
            this.lastSyncMillis = lastSyncMillis;
        }
    }
}
