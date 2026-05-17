package com.common.business.mask;

import com.common.business.vo.LoginUser;

import java.util.Collection;
import java.util.Set;

/**
 * 脱敏框架的权限解析 SPI
 *
 * <p>用于从当前 {@link LoginUser} 解析出该用户拥有的"看明文"权限码集合。
 * {@link com.common.business.mask.core.MaskCore} 调用该 SPI 决定字段是否豁免脱敏。</p>
 *
 * <p><b>项目背景</b>：网关用 {@link LoginUser#simpleLoginUser(LoginUser)} 把用户信息压缩进
 * HTTP Header 转发到下游，<b>故意丢弃了 {@code permissionList} 字段</b>（避免撑爆 Header）。
 * 因此下游业务服务里 {@code user.getPermissionList()} 通常是 {@code null}，必须靠本 SPI
 * 从 sys 服务现查（参考 {@code DataPermissionAspect} 的做法）。</p>
 *
 * <h3>实现选型</h3>
 * <ul>
 *   <li><b>默认</b>：{@code LoginUserMaskPermissionResolver} —— 直接读 {@code user.getPermissionList()}，
 *       仅适用于"上游已填好 permissionList"的场景（如单测、网关侧自身、特殊定制 Filter）。</li>
 *   <li><b>推荐</b>：业务侧引入 {@code erp-rpc-sys} 时自动启用 {@code FeignMaskPermissionResolver}，
 *       通过 {@code SysUserFeign} 现查 + Caffeine 本地缓存 + Redis Pub/Sub 主动失效，
 *       既保证下游能拿到权限，又避免每请求 1 次 Feign 把 sys 打爆。</li>
 * </ul>
 *
 * <h3>失效一致性</h3>
 * <p>本接口的实现可选择性订阅 {@link com.common.business.constant.RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}，
 * 由 sys 在 {@code sys_user_role / sys_role_menu / sys_user} 写入路径广播失效，达到全集群 100ms 内一致。</p>
 *
 * @author cloud-erp
 */
public interface MaskPermissionResolver {

    /**
     * 返回当前用户的"看明文"权限码集合
     *
     * <p>实现要求：</p>
     * <ul>
     *   <li>必须非空返回。无权限或解析失败请返回 {@code Collections.emptySet()}，不要返回 null。</li>
     *   <li>必须支持 {@code user == null} 的入参（匿名 / 系统线程），返回空集合即可。</li>
     *   <li>外部高频路径每次调用，实现需自带缓存避免雪崩（Feign 调用 / DB 查询都属于昂贵操作）。</li>
     * </ul>
     */
    Set<String> resolve(LoginUser user);

    /**
     * 失效指定 uid 集合的本地缓存（如果实现带缓存）
     *
     * <p>由 {@code MaskPermissionEvictListener} 在收到 Redis Pub/Sub 失效消息时调用。
     * 无缓存的实现（如 {@code LoginUserMaskPermissionResolver}）默认 no-op。</p>
     *
     * <p>实现要求：</p>
     * <ul>
     *   <li>幂等：同样的 uid 集合多次调用，行为一致</li>
     *   <li>容错：不允许抛异常（listener 不会做异常处理；抛出会导致后续消息处理被影响）</li>
     *   <li>支持 {@code uids == null} 或空集合，直接返回</li>
     * </ul>
     */
    default void evict(Collection<String> uids) {
        // 默认 no-op：无缓存的实现无需失效
    }

    /**
     * 失效全部本地缓存（如果实现带缓存）
     *
     * <p>用于"角色 / 菜单大改"等无法精确定位用户的写入场景。
     * 无缓存的实现默认 no-op。</p>
     */
    default void evictAll() {
        // 默认 no-op：无缓存的实现无需失效
    }
}
