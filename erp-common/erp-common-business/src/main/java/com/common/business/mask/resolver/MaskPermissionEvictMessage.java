package com.common.business.mask.resolver;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 脱敏权限缓存失效广播消息
 *
 * <p>通过 {@link com.common.business.constant.RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}
 * 在 sys 服务和业务节点之间 Pub/Sub 投递。</p>
 *
 * <h3>消息语义</h3>
 * <ul>
 *   <li>{@link Type#USER}：精确失效指定 uid 集合；最常用，对应 {@code sys_user_role} 改动。</li>
 *   <li>{@link Type#ALL}：清空整个本地权限缓存；用于"角色权限码大改"等无法精确定位用户的写入
 *       （如 {@code sys_role_menu} 改动，不知道是哪些 uid 持有该角色）。</li>
 * </ul>
 *
 * <p><b>故意不引入 ROLE 粒度</b>：业务节点不存"角色→用户"反向索引，
 * 若细化到 role 粒度需要每个节点反查 sys，得不偿失。{@link Type#ALL} 直接清空更简单且廉价
 * （清空后下次访问 lazy 重建）。</p>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class MaskPermissionEvictMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Type type;

    private Set<String> uids;

    /**
     * 来源标识，便于排查（如 sys 服务名、操作来源）
     */
    private String source;

    private long ts;

    public enum Type {
        USER,
        ALL
    }

    public static MaskPermissionEvictMessage user(Collection<String> uids, String source) {
        MaskPermissionEvictMessage m = new MaskPermissionEvictMessage();
        m.setType(Type.USER);
        m.setUids(uids == null ? Collections.emptySet() : new LinkedHashSet<>(uids));
        m.setSource(source);
        m.setTs(System.currentTimeMillis());
        return m;
    }

    public static MaskPermissionEvictMessage all(String source) {
        MaskPermissionEvictMessage m = new MaskPermissionEvictMessage();
        m.setType(Type.ALL);
        m.setUids(Collections.emptySet());
        m.setSource(source);
        m.setTs(System.currentTimeMillis());
        return m;
    }
}
