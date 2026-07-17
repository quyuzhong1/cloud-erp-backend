package com.common.business.mask.cache;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.protect.MaskProtectBinding;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置 Redis cache-aside 访问器。
 *
 * <p>本类不再持有 cfg_mask_field 全量本地快照。读路径以 Redis Bucket 为准；
 * Redis miss 时通过 {@link CfgMaskFieldCacheLoader} 回源 sys，再把全量数据写回 Redis。
 * 本地只保留最近观察到的版本号，用于提醒类元数据缓存失效。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskFieldLocalCache {

    private static final long VERSION_CHECK_INTERVAL_MS = 1000L;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private ObjectProvider<CfgMaskFieldCacheLoader> loaderProvider;

    private volatile long observedVersion;
    private volatile long lastCheckMillis;

    /**
     * 兼容旧调用：返回该字段排序后的第一条规则。
     */
    public CfgMaskFieldSnapshotEntry get(String classPath, String fieldName) {
        List<CfgMaskFieldSnapshotEntry> rules = getRules(classPath, fieldName);
        return rules.isEmpty() ? null : rules.get(0);
    }

    /**
     * 查询某个字段的所有未禁用规则，按 sort ASC 返回。
     */
    public List<CfgMaskFieldSnapshotEntry> getRules(String classPath, String fieldName) {
        if (classPath == null || fieldName == null) {
            return Collections.emptyList();
        }
        CfgMaskFieldFullCacheDTO payload = loadPayload();
        List<CfgMaskFieldSnapshotEntry> data = payload == null ? null : payload.getData();
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        List<CfgMaskFieldSnapshotEntry> result = new ArrayList<>();
        for (CfgMaskFieldSnapshotEntry entry : data) {
            if (!usable(entry)) {
                continue;
            }
            if (classPath.equals(entry.getClassPath()) && fieldName.equals(entry.getFieldName())) {
                result.add(entry);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        result.sort(Comparator
                .comparingInt((CfgMaskFieldSnapshotEntry e) -> e.getSort() == null ? 0 : e.getSort())
                .thenComparing(e -> e.getStrategy() == null ? "" : e.getStrategy().name()));
        return Collections.unmodifiableList(result);
    }

    public List<CfgMaskFieldSnapshotEntry> getValueProtectRules(String classPath, String fieldName) {
        if (classPath == null || fieldName == null) {
            return Collections.emptyList();
        }
        CfgMaskFieldFullCacheDTO payload = loadPayload();
        List<CfgMaskFieldSnapshotEntry> data = payload == null ? null : payload.getData();
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        List<CfgMaskFieldSnapshotEntry> result = new ArrayList<>();
        for (CfgMaskFieldSnapshotEntry entry : data) {
            if (!usable(entry) || !entry.isValueProtectEnabled()) {
                continue;
            }
            if (findBinding(entry, classPath, fieldName) != null) {
                result.add(entry);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        result.sort(Comparator
                .comparingInt((CfgMaskFieldSnapshotEntry e) -> e.getSort() == null ? 0 : e.getSort())
                .thenComparing(e -> e.getClassPath() == null ? "" : e.getClassPath()));
        return Collections.unmodifiableList(result);
    }

    void setRedissonClientForTest(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    void setLoaderProviderForTest(ObjectProvider<CfgMaskFieldCacheLoader> loaderProvider) {
        this.loaderProvider = loaderProvider;
    }

    public MaskProtectBinding findProtectBinding(CfgMaskFieldSnapshotEntry entry,
                                                 String classPath, String fieldName) {
        return findBinding(entry, classPath, fieldName);
    }

    /**
     * 供 {@code MaskClassDescriptorRegistry} 在读取类元数据前做轻量版本检查。
     *
     * <p>为避免每个 POJO 都打 Redis，本方法最多每秒检查一次。发现 Redis miss 会主动回源并回填，
     * 发现版本变化则返回 true，由注册中心清理类元数据缓存。</p>
     */
    public boolean refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
            return false;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
                return false;
            }
            lastCheckMillis = now;
            CfgMaskFieldFullCacheDTO payload = loadPayload();
            long version = payload == null ? 0L : payload.getVersion();
            if (version == observedVersion) {
                return false;
            }
            observedVersion = version;
            log.info("CfgMaskField redis cache version changed, version={}", version);
            return true;
        }
    }

    /**
     * 运维视图：读取 Redis 中当前全量缓存，不暴露任何本地全量快照。
     */
    public Map<String, Object> snapshotView() {
        CfgMaskFieldFullCacheDTO payload = loadPayload();
        List<CfgMaskFieldSnapshotEntry> data = payload == null ? Collections.emptyList() : payload.getData();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("cacheMode", "redis-cache-aside");
        view.put("redisKey", RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY);
        view.put("version", payload == null ? 0L : payload.getVersion());
        view.put("size", data == null ? 0 : data.size());
        view.put("observedVersion", observedVersion);
        view.put("data", data == null ? Collections.emptyList() : data);
        return view;
    }

    private CfgMaskFieldFullCacheDTO loadPayload() {
        RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY);
        String body = null;
        try {
            body = bucket.get();
        } catch (Throwable e) {
            log.warn("CfgMaskField redis get failed, fallback to loader, msg={}", e.getMessage());
        }
        if (body != null && !body.isEmpty()) {
            try {
                return JSON.parseObject(body, CfgMaskFieldFullCacheDTO.class);
            } catch (Throwable e) {
                log.warn("CfgMaskField redis payload parse failed, fallback to loader, msg={}", e.getMessage());
            }
        }
        CfgMaskFieldFullCacheDTO loaded = loadFromSys();
        if (loaded != null) {
            try {
                bucket.set(JSON.toJSONString(loaded));
            } catch (Throwable e) {
                log.warn("CfgMaskField redis set after loader failed, msg={}", e.getMessage());
            }
        }
        return loaded;
    }

    private CfgMaskFieldFullCacheDTO loadFromSys() {
        CfgMaskFieldCacheLoader loader = loaderProvider.getIfAvailable();
        if (loader == null) {
            return null;
        }
        CfgMaskFieldFullCacheDTO loaded = loader.load();
        return loaded == null ? null : loaded;
    }

    private static boolean usable(CfgMaskFieldSnapshotEntry entry) {
        return entry != null
                && entry.getClassPath() != null
                && entry.getFieldName() != null
                && entry.getStrategy() != null
                && entry.getStrategy() != MaskStrategy.AUTO_FROM_CONFIG;
    }

    private static MaskProtectBinding findBinding(CfgMaskFieldSnapshotEntry entry,
                                                  String classPath, String fieldName) {
        if (entry == null || classPath == null || fieldName == null
                || entry.getProtectParamBindings() == null) {
            return null;
        }
        for (MaskProtectBinding binding : entry.getProtectParamBindings()) {
            if (binding == null || binding.getParamClassPath() == null || binding.getParamClassPath().isEmpty()) {
                continue;
            }
            String bindingField = binding.getParamFieldName() == null || binding.getParamFieldName().isEmpty()
                    ? entry.getFieldName() : binding.getParamFieldName();
            if (classPath.equals(binding.getParamClassPath()) && fieldName.equals(bindingField)) {
                return binding;
            }
        }
        return null;
    }

}
