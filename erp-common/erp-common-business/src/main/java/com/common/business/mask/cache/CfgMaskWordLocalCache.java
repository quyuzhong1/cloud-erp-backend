package com.common.business.mask.cache;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 词典本地全量快照
 *
 * <p>架构与 {@link CfgMaskFieldLocalCache} 一致：</p>
 * <ul>
 *   <li>启动时从 Redis Bucket {@link RedisCacheConstants#MASK_WORD_CFG_FULL_KEY} 同步一次冷启动数据；</li>
 *   <li>运行期订阅 {@link RedisCacheConstants#MASK_WORD_CFG_REFRESH_CHANNEL}（详见
 *       {@link com.common.business.mask.listener.CfgMaskWordRefreshListener}）；</li>
 *   <li>{@link #apply(CfgMaskWordFullCacheDTO)} 通过 {@code version} 单调递增校验自动忽略乱序消息；</li>
 *   <li>每次成功 apply 后做"差量计算"，把变化部分增量灌入 {@link SensitiveWordBs}，
 *       避免每次刷新都重建整棵 DFA。</li>
 * </ul>
 *
 * <p>{@link SensitiveWordBs} 通过 {@link ObjectProvider} 懒注入：缓存先于引擎初始化时不报错，
 * 引擎 Bean 就绪后由 {@link com.common.business.mask.config.SensitiveWordBsAutoConfiguration} 在
 * {@code @PostConstruct} 阶段重新触发 {@link #replay()} 一次。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskWordLocalCache {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用 ObjectProvider 防止 SensitiveWordBs 还没装配时（启动早期）报循环依赖。
     * 真实使用时按需取一次。
     */
    @Resource
    private ObjectProvider<SensitiveWordBs> sensitiveWordBsProvider;

    /**
     * 当前生效的全量快照（不可变 Set + 版本号 + 最近一次同步时间）。
     */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    /**
     * 应用一次全量推送 / 持久化的词典；version 单调递增校验，忽略乱序/旧消息。
     * apply 成功后按差量增量更新 {@link SensitiveWordBs}。
     */
    public synchronized void apply(CfgMaskWordFullCacheDTO payload) {
        if (payload == null || payload.getVersion() <= snapshot.version) {
            return;
        }
        Set<String> nextDeny = new HashSet<>();
        Set<String> nextAllow = new HashSet<>();
        List<CfgMaskWordSnapshotEntry> data = payload.getData();
        if (data != null) {
            for (CfgMaskWordSnapshotEntry e : data) {
                if (e == null || StringUtils.isBlank(e.getWord()) || e.getWordType() == null) {
                    continue;
                }
                int type = e.getWordType();
                if (type == CfgMaskWordSnapshotEntry.WORD_TYPE_DENY) {
                    nextDeny.add(e.getWord());
                } else if (type == CfgMaskWordSnapshotEntry.WORD_TYPE_ALLOW) {
                    nextAllow.add(e.getWord());
                }
            }
        }
        Snapshot prev = this.snapshot;
        Snapshot next = new Snapshot(
                Collections.unmodifiableSet(nextDeny),
                Collections.unmodifiableSet(nextAllow),
                payload.getVersion(),
                System.currentTimeMillis()
        );
        this.snapshot = next;

        applyDiffToEngine(prev, next);
        log.info("CfgMaskWordLocalCache apply ok, denySize={}, allowSize={}, version={}",
                next.deny.size(), next.allow.size(), payload.getVersion());
    }

    /**
     * 引擎完成构造后，把当前快照里的全量词重放给引擎。
     * 用于"缓存先 apply、引擎后初始化"场景；幂等。
     */
    public synchronized void replay() {
        Snapshot s = this.snapshot;
        if (s == Snapshot.EMPTY) {
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
            log.info("CfgMaskWordLocalCache replay ok, denySize={}, allowSize={}",
                    s.deny.size(), s.allow.size());
        } catch (Throwable e) {
            log.warn("CfgMaskWordLocalCache replay to SensitiveWordBs failed", e);
        }
    }

    /** 当前黑名单视图（不可变）。 */
    public Set<String> denyWords() {
        return snapshot.deny;
    }

    /** 当前白名单视图（不可变）。 */
    public Set<String> allowWords() {
        return snapshot.allow;
    }

    /**
     * 当前快照运维视图：version / size / lastSyncMillis / data
     */
    public Map<String, Object> snapshotView() {
        Snapshot s = this.snapshot;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("version", s.version);
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
            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_WORD_CFG_FULL_KEY);
            String body = bucket.get();
            if (body == null || body.isEmpty()) {
                log.info("CfgMaskWordLocalCache init: redis bucket empty, wait for next broadcast");
                return;
            }
            CfgMaskWordFullCacheDTO payload = JSON.parseObject(body, CfgMaskWordFullCacheDTO.class);
            apply(payload);
        } catch (Throwable e) {
            log.warn("CfgMaskWordLocalCache init load from redis bucket failed, wait for next broadcast", e);
        }
    }

    /**
     * 差量更新到 SensitiveWordBs：
     * <ul>
     *   <li>需要新增的：next 有但 prev 没有；</li>
     *   <li>需要删除的：prev 有但 next 没有；</li>
     * </ul>
     * 失败时仅打日志：本地 snapshot 已切换，重启 / 下次 replay 会重新对齐。
     */
    private void applyDiffToEngine(Snapshot prev, Snapshot next) {
        SensitiveWordBs bs = sensitiveWordBsProvider.getIfAvailable();
        if (bs == null) {
            // 引擎还没装配；等引擎 Bean 启动后由 SensitiveWordBsAutoConfiguration 调 replay() 全量重放
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
            log.warn("CfgMaskWordLocalCache applyDiffToEngine failed, snapshot still updated", e);
        }
    }

    private static List<String> diff(Set<String> minuend, Set<String> subtrahend) {
        if (minuend.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String s : minuend) {
            if (!subtrahend.contains(s)) {
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
            this.deny = deny;
            this.allow = allow;
            this.version = version;
            this.lastSyncMillis = lastSyncMillis;
        }
    }
}
