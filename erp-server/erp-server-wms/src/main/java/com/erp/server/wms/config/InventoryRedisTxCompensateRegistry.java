package com.erp.server.wms.config;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.common.business.utils.AbstractRedisUtil;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.server.wms.utils.InventoryRedisUtil;
import com.erp.server.wms.utils.VirtualInventoryRedisUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 库存事务补偿计时登记（Redis 持久化）：孤儿 TRY 回滚、DB 已提交但 Redis commit 失败的重试。
 * <p>key 格式：{@code inventory:tx:compensate:{entity|virtual}:{commit|rollback}:{transactionId}}，值为首次发现时间戳（毫秒）。</p>
 */
@Slf4j
@Component
public class InventoryRedisTxCompensateRegistry {

    /** commit 重试登记默认 TTL（秒） */
    public static final long DEFAULT_COMMIT_KEY_TTL_SECONDS = 86400L;

    /** 孤儿 rollback 登记默认 TTL（秒） */
    public static final long DEFAULT_ROLLBACK_KEY_TTL_SECONDS = 172800L;

    private static final String KEY_PREFIX = "inventory:tx:compensate";

    public enum TxKind {
        ENTITY,
        VIRTUAL
    }

    @Resource
    private InventoryRedisUtil inventoryRedisUtil;

    @Resource
    private VirtualInventoryRedisUtil virtualInventoryRedisUtil;

    /**
     * 记录首次发现待补偿事务；已存在则不覆盖首次时间。
     *
     * @param kind          实体仓或虚拟仓
     * @param transactionId Redis 事务 ID
     * @param rollback      true 登记孤儿 TRY 回滚；false 登记 commit 重试
     */
    public void touchPending(TxKind kind, String transactionId, boolean rollback) {
        if (StringUtils.isBlank(transactionId)) {
            return;
        }
        String key = buildKey(kind, rollback, transactionId);
        AbstractRedisUtil redisUtil = resolveRedisUtil(kind);
        if (redisUtil.get(key) != null) {
            return;
        }
        long ttlSeconds = rollback ? DEFAULT_ROLLBACK_KEY_TTL_SECONDS : DEFAULT_COMMIT_KEY_TTL_SECONDS;
        redisUtil.set(key, String.valueOf(System.currentTimeMillis()), ttlSeconds);
    }

    /**
     * Redis 回调 commit 失败时立即登记，便于 Job 在较短 timeout 后补偿。
     *
     * @param kind          实体仓或虚拟仓
     * @param transactionId Redis 事务 ID
     */
    public void markCommitRetryFailed(TxKind kind, String transactionId) {
        touchPending(kind, transactionId, false);
    }

    /**
     * 是否已超过等待时间，可执行补偿。
     *
     * @param kind           实体仓或虚拟仓
     * @param transactionId  Redis 事务 ID
     * @param timeoutSeconds 自首次登记起的等待秒数
     * @param rollback       true 检查孤儿回滚；false 检查 commit 重试
     * @return 超时后可执行补偿
     */
    public boolean isDue(TxKind kind, String transactionId, int timeoutSeconds, boolean rollback) {
        if (StringUtils.isBlank(transactionId)) {
            return false;
        }
        Object value = resolveRedisUtil(kind).get(buildKey(kind, rollback, transactionId));
        if (value == null) {
            return false;
        }
        try {
            long firstSeen = Long.parseLong(value.toString());
            return System.currentTimeMillis() - firstSeen >= timeoutSeconds * 1000L;
        } catch (NumberFormatException e) {
            log.warn("补偿登记时间戳非法 kind={} transactionId={} value={}", kind, transactionId, value);
            return false;
        }
    }

    /**
     * 列出仍有效的 commit 重试补偿登记 transactionId（不依赖 TRANSACTION key 是否存在）。
     *
     * @param kind 实体仓或虚拟仓
     * @return commit 补偿登记中的 transactionId 集合
     */
    public Set<String> listPendingCommitRetryTransactionIds(TxKind kind) {
        String pattern = buildKeyPattern(kind, false);
        java.util.Collection<String> keys = resolveRedisUtil(kind).keys(pattern);
        if (CollUtil.isEmpty(keys)) {
            return Collections.emptySet();
        }
        return keys.stream()
                .map(InventoryRedisTxCompensateRegistry::extractTransactionIdFromKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 判断 Redis TRANSACTION TRY key 是否仍存在。
     *
     * @param kind          实体仓或虚拟仓
     * @param transactionId Redis 事务 ID
     * @return TRANSACTION key 是否存在
     */
    public boolean hasRedisTransactionKey(TxKind kind, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return false;
        }
        String transactionKey = InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, transactionId);
        return resolveRedisUtil(kind).hasKey(transactionKey);
    }

    /**
     * 补偿成功后清理登记。
     *
     * @param kind          实体仓或虚拟仓
     * @param transactionId Redis 事务 ID
     * @param rollback      true 清理孤儿回滚登记；false 清理 commit 重试登记
     */
    public void clearPending(TxKind kind, String transactionId, boolean rollback) {
        if (StringUtils.isBlank(transactionId)) {
            return;
        }
        resolveRedisUtil(kind).del(buildKey(kind, rollback, transactionId));
    }

    /**
     * 清理孤儿 rollback 补偿登记：仅当 Redis TRANSACTION key 已不存在时删除 rollback 登记。
     * commit 重试登记不在此清理，依赖 TTL 或 {@link #clearPending} 成功路径删除。
     *
     * @param kind                 实体仓或虚拟仓
     * @param activeTransactionIds 当前仍存在的 Redis TRANSACTION ID 集合
     */
    public void pruneStale(TxKind kind, java.util.Set<String> activeTransactionIds) {
        pruneStaleByOperation(kind, activeTransactionIds, true);
    }

    private void pruneStaleByOperation(TxKind kind, java.util.Set<String> activeTransactionIds, boolean rollback) {
        String pattern = buildKeyPattern(kind, rollback);
        java.util.Collection<String> keys = resolveRedisUtil(kind).keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            String transactionId = extractTransactionIdFromKey(key);
            if (CharSequenceUtil.isBlank(transactionId) || activeTransactionIds.contains(transactionId)) {
                continue;
            }
            resolveRedisUtil(kind).del(key);
        }
    }

    private AbstractRedisUtil resolveRedisUtil(TxKind kind) {
        return TxKind.ENTITY.equals(kind) ? inventoryRedisUtil : virtualInventoryRedisUtil;
    }

    private static String buildKey(TxKind kind, boolean rollback, String transactionId) {
        return CharSequenceUtil.format("{}:{}:{}:{}",
                KEY_PREFIX,
                kind.name().toLowerCase(),
                rollback ? "rollback" : "commit",
                transactionId);
    }

    private static String buildKeyPattern(TxKind kind, boolean rollback) {
        return CharSequenceUtil.format("{}:{}:{}:*",
                KEY_PREFIX,
                kind.name().toLowerCase(),
                rollback ? "rollback" : "commit");
    }

    private static String extractTransactionIdFromKey(String key) {
        if (CharSequenceUtil.isBlank(key)) {
            return "";
        }
        int lastColon = key.lastIndexOf(':');
        return lastColon < 0 ? key : key.substring(lastColon + 1);
    }
}
