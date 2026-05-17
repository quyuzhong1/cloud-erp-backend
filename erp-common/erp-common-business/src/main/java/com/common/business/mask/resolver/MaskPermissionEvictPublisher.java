package com.common.business.mask.resolver;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
 * <p><b>调用时机</b>：建议放在事务提交后（如 {@code TransactionSynchronizationManager#registerSynchronization}），
 * 避免事务回滚后发出无效失效，但为了实现简单，目前调用方可以直接在写完后调，
 * 多发一次失效广播的代价远小于"漏发导致的数据泄露"。</p>
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
}
