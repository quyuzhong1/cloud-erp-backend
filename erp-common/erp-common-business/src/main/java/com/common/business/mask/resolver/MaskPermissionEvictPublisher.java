package com.common.business.mask.resolver;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏权限失效广播 publisher
 *
 * <p>由 sys 服务在用户/角色/菜单等"会影响某用户权限码集合"的写入路径调用：
 * <ul>
 *   <li>{@code sys_user_role} 改动 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_role_menu} 改动 → {@link #publishAll(String)}（无法精确定位 uid）</li>
 *   <li>{@code sys_user} 禁用 / 角色字段重置 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_menu} 改动（权限码改名 / 删除）→ {@link #publishAll(String)}</li>
 * </ul>
 *
 * <p>本 publisher 是<b>非阻塞、容错</b>的：Redis 不可用时只记 warn，不影响主业务事务。
 * 失效消息丢失由业务节点本地 TTL 兜底，最终一致。</p>
 *
 * <p><b>调用时机</b>：事务写路径使用 afterCommit 方法，避免回滚事务提前广播后，
 * 其他请求在提交前重新加载旧权限并形成脏缓存。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskPermissionEvictPublisher {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 失效指定 uid 集合的权限缓存（最常用，精确）
     */
    public void publishUser(Collection<String> uids, String source) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        try {
            MaskPermissionEvictMessage msg = MaskPermissionEvictMessage.user(uids, source);
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_PERM_EVICT_CHANNEL);
            long received = topic.publish(JSON.toJSONString(msg));
            log.info("MaskPermissionEvictPublisher publish USER uids={}, source={}, received={}",
                    uids.size(), source, received);
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictPublisher publish USER failed, source={}, msg={}",
                    source, e.getMessage());
        }
    }

    /**
     * 失效单个 uid 的便捷重载
     */
    public void publishUser(String uid, String source) {
        if (uid == null || uid.isEmpty()) {
            return;
        }
        publishUser(Collections.singleton(uid), source);
    }

    /**
     * 事务提交后失效指定 uid；当前无事务时立即广播。
     */
    public void publishUserAfterCommit(Collection<String> uids, String source) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        Collection<String> snapshot = new ArrayList<>(uids);
        runAfterCommit(() -> publishUser(snapshot, source));
    }

    /**
     * 清空全部本地权限缓存（重操作，慎用：用于角色/菜单大改）
     */
    public void publishAll(String source) {
        try {
            MaskPermissionEvictMessage msg = MaskPermissionEvictMessage.all(source);
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_PERM_EVICT_CHANNEL);
            long received = topic.publish(JSON.toJSONString(msg));
            log.info("MaskPermissionEvictPublisher publish ALL, source={}, received={}",
                    source, received);
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictPublisher publish ALL failed, source={}, msg={}",
                    source, e.getMessage());
        }
    }

    /**
     * 事务提交后清空全部权限缓存；当前无事务时立即广播。
     */
    public void publishAllAfterCommit(String source) {
        runAfterCommit(() -> publishAll(source));
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
