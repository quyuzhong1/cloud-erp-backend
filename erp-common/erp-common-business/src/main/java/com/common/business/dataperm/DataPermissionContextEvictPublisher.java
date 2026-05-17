package com.common.business.dataperm;

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
 * 数据权限上下文失效广播 publisher
 *
 * <p>由 sys 服务在"会影响某用户数据权限上下文"的写入路径调用：
 * <ul>
 *   <li>{@code sys_role_user} 改动 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_department_user} 改动 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_user_shop} 改动 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_user_warehouse} 改动 → {@link #publishUser(java.util.Collection, String)}</li>
 *   <li>{@code sys_role_menu / sys_role / sys_menu} 改动 → {@link #publishAll(String)}（无法精确定位 uid）</li>
 *   <li>{@code sys_user} 禁用 → {@link #publishUser(java.util.Collection, String)}</li>
 * </ul>
 *
 * <p>设计同 {@code MaskPermissionEvictPublisher}：非阻塞、容错；Redis 不可用时只记 warn，
 * 不影响主业务事务。失效消息丢失由业务节点本地 60s TTL 兜底，最终一致。</p>
 *
 * <p>独立于 mask publisher：两者 channel 不同、消息独立、互不耦合。某些写入路径会同时
 * 调用两个 publisher（典型如 {@code sys_role_menu}：mask 关心 permissionsCode 集合，
 * dataPerm 也关心 permissionsList，两边都要 publishAll）。</p>
 *
 * <p><b>调用时机</b>：与 mask 一致，目前直接在写完后调，多发一次失效广播代价远小于"漏发导致的数据权限越权"。
 * 如未来发现回滚后误失效频繁，可统一改为事务提交后 {@code TransactionSynchronizationManager} 触发。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class DataPermissionContextEvictPublisher {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 失效指定 uid 集合的数据权限上下文缓存（最常用，精确）
     */
    public void publishUser(Collection<String> uids, String source) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        try {
            DataPermissionContextEvictMessage msg = DataPermissionContextEvictMessage.user(uids, source);
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL);
            long received = topic.publish(JSON.toJSONString(msg));
            log.info("DataPermissionContextEvictPublisher publish USER uids={}, source={}, received={}",
                    uids.size(), source, received);
        } catch (Throwable e) {
            log.warn("DataPermissionContextEvictPublisher publish USER failed, source={}, msg={}",
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
     * 清空全部本地数据权限上下文缓存（重操作，慎用：用于角色/菜单/角色用户大改）
     */
    public void publishAll(String source) {
        try {
            DataPermissionContextEvictMessage msg = DataPermissionContextEvictMessage.all(source);
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL);
            long received = topic.publish(JSON.toJSONString(msg));
            log.info("DataPermissionContextEvictPublisher publish ALL, source={}, received={}",
                    source, received);
        } catch (Throwable e) {
            log.warn("DataPermissionContextEvictPublisher publish ALL failed, source={}, msg={}",
                    source, e.getMessage());
        }
    }
}
