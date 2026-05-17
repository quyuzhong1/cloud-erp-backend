package com.common.business.dataperm;

import com.common.business.constant.RedisCacheConstants;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 数据权限上下文失效广播消息
 *
 * <p>通过 {@link RedisCacheConstants#DATA_PERM_CTX_EVICT_CHANNEL} 在 sys 服务和业务节点之间 Pub/Sub 投递。
 * 与 {@code MaskPermissionEvictMessage} 同构，两者独立 channel、独立 listener，互不耦合。</p>
 *
 * <h3>消息语义</h3>
 * <ul>
 *   <li>{@link Type#USER}：精确失效指定 uid 集合；最常用，对应
 *       {@code sys_user_role / sys_department_user / sys_user_shop / sys_user_warehouse} 改动。</li>
 *   <li>{@link Type#ALL}：清空整个本地数据权限上下文缓存；用于"角色被删 / 角色菜单大改"等无法精确定位用户的写入。</li>
 * </ul>
 *
 * <p>同 mask 方案不引入 ROLE 粒度：业务节点不存"角色→用户"反向索引，
 * 若细化到 role 粒度需要每个节点反查 sys，得不偿失。{@link Type#ALL} 整体清空更简单且廉价
 * （下次访问 lazy 重建）。</p>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class DataPermissionContextEvictMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Type type;

    private Set<String> uids;

    /**
     * 来源标识，便于排查（如 service 名 + 方法名）
     */
    private String source;

    private long ts;

    public enum Type {
        USER,
        ALL
    }

    public static DataPermissionContextEvictMessage user(Collection<String> uids, String source) {
        DataPermissionContextEvictMessage m = new DataPermissionContextEvictMessage();
        m.setType(Type.USER);
        m.setUids(uids == null ? Collections.emptySet() : new LinkedHashSet<>(uids));
        m.setSource(source);
        m.setTs(System.currentTimeMillis());
        return m;
    }

    public static DataPermissionContextEvictMessage all(String source) {
        DataPermissionContextEvictMessage m = new DataPermissionContextEvictMessage();
        m.setType(Type.ALL);
        m.setUids(Collections.emptySet());
        m.setSource(source);
        m.setTs(System.currentTimeMillis());
        return m;
    }
}
