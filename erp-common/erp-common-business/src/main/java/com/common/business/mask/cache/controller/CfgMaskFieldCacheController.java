package com.common.business.mask.cache.controller;

import com.common.business.mask.MaskPermissionResolver;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.core.MaskClassDescriptorRegistry;
import com.common.business.mask.resolver.MaskPermissionEvictPublisher;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字段脱敏配置本地缓存查询接口
 *
 * <p>放在 {@code erp-common-business} 下，所有依赖该 common 模块的业务服务自动暴露此接口。
 * 用途：排查"某个 Pod 本地缓存是否被正确填充 / 版本号是否陈旧 / 类元数据缓存命中数"。</p>
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("maskFieldCfgCache")
public class CfgMaskFieldCacheController extends BaseController {

    @Resource
    private CfgMaskFieldLocalCache localCache;

    @Autowired(required = false)
    private MaskClassDescriptorRegistry descriptorRegistry;

    /**
     * 权限解析 SPI；业务侧若引入 {@code erp-rpc-sys} 会自动注入 FeignMaskPermissionResolver
     * （它带 Caffeine 本地缓存 + Pub/Sub 主动失效）。required=false 兼容老服务。
     */
    @Autowired(required = false)
    private MaskPermissionResolver permissionResolver;

    /**
     * 权限失效广播 publisher；required=false 兼容尚未引入 Redisson 的特殊服务。
     */
    @Autowired(required = false)
    private MaskPermissionEvictPublisher permissionEvictPublisher;

    /**
     * 返回当前节点本地缓存的全量配置快照
     *
     * <p>响应字段：</p>
     * <ul>
     *   <li>version：快照版本号（System.currentTimeMillis），0 表示未被填充过</li>
     *   <li>size：当前快照内 (className#fieldName) 配置条数</li>
     *   <li>lastSyncMillis：本节点最近一次成功 apply 的时间戳，0 表示未 apply 过</li>
     *   <li>data：(className#fieldName) -&gt; CfgMaskFieldSnapshotEntry 明细</li>
     *   <li>descriptorCacheSize：类元数据已缓存的类数量</li>
     * </ul>
     */
    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        Map<String, Object> view = new LinkedHashMap<>(localCache.snapshotView());
        view.put("descriptorCacheSize", descriptorRegistry == null ? 0 : descriptorRegistry.size());
        return success(view);
    }

    /**
     * 诊断接口：返回当前线程 {@link UserContext} 解析出的 LoginUser 关键信息
     *
     * <p>用于排查"权限配了但还是被脱敏"类问题。响应只返回判权所需的最小字段，
     * 不包含 token / mobile / 真实姓名等敏感数据。</p>
     *
     * <p>典型用法：</p>
     * <pre>GET /maskFieldCfgCache/whoami?probe=plm:operateLog:content:plain</pre>
     *
     * <p>关键字段：</p>
     * <ul>
     *   <li>loginUserNull：true 表示当前线程根本没拿到 LoginUser（网关 / Filter / RPC 链路问题）</li>
     *   <li>isUserSystem：true 表示走的 system 兜底用户，无 permissionList / isSupper</li>
     *   <li>isSupper：超管标志</li>
     *   <li>permissionListSize：权限码总数</li>
     *   <li>hasProbe：传入 probe 参数时，回报该权限码是否命中</li>
     *   <li>permissionListSample：前 50 条权限码预览（仅用于人工核对）</li>
     * </ul>
     */
    @GetMapping("/whoami")
    public ApiResult<Map<String, Object>> whoami(@RequestParam(required = false) String probe) {
        Map<String, Object> view = new LinkedHashMap<>();
        Boolean isUserSystem = UserContext.getIsUserSystem();
        view.put("isUserSystem", isUserSystem);
        LoginUser user = UserContext.getLoginUser();
        view.put("loginUserNull", user == null);
        if (user == null) {
            return success(view);
        }
        view.put("uid", user.getUid());
        view.put("userName", user.getUserName());
        view.put("isSupper", user.getIsSupper());

        // 1. LoginUser 原始 permissionList（项目里下游服务通常是 null，因为 simpleLoginUser 故意丢弃）
        List<String> perms = user.getPermissionList();
        view.put("permissionListNull", perms == null);
        view.put("permissionListSize", perms == null ? 0 : perms.size());

        // 2. MaskCore 实际使用的权限集合（经 resolver 解析，可能命中 Feign 缓存）
        //    这是排查"权限配了但还是脱敏"问题的关键 —— 看 resolver 究竟拿到了什么
        Set<String> resolved = Collections.emptySet();
        if (permissionResolver != null) {
            try {
                resolved = permissionResolver.resolve(user);
                if (resolved == null) {
                    resolved = Collections.emptySet();
                }
            } catch (Throwable e) {
                view.put("resolverError", e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        }
        view.put("resolverClass", permissionResolver == null ? null
                : permissionResolver.getClass().getSimpleName());
        view.put("resolvedPermissionsSize", resolved.size());

        if (probe != null && !probe.isEmpty()) {
            view.put("probe", probe);
            view.put("hasProbeInLoginUser", perms != null && perms.contains(probe));
            view.put("hasProbeInResolved", resolved.contains(probe));
        }
        if (perms != null && !perms.isEmpty()) {
            view.put("permissionListSample", perms.stream().limit(50).collect(Collectors.toList()));
        }
        if (!resolved.isEmpty()) {
            view.put("resolvedPermissionsSample", resolved.stream().limit(50).collect(Collectors.toList()));
        }
        return success(view);
    }

    /**
     * 兜底失效接口：手动 publish 权限缓存失效广播
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>运维直接 SQL 改了 sys_role_menu / sys_user_role，绕过了 service 自动 publish</li>
     *   <li>怀疑 sys 服务那侧的 publisher 没生效，想强制刷一次</li>
     *   <li>FeignMaskPermissionResolver 拿到了脏数据，先失效再让其重新拉</li>
     * </ul>
     *
     * <h3>用法</h3>
     * <pre>
     * POST /maskFieldCfgCache/evictPermission?uids=u1,u2,u3        # 精确失效几个 uid
     * POST /maskFieldCfgCache/evictPermission?all=true             # 整体清空（重操作）
     * </pre>
     *
     * <p>两个参数二选一；同时传时 all=true 优先。</p>
     */
    @PostMapping("/evictPermission")
    public ApiResult<Map<String, Object>> evictPermission(@RequestParam(required = false) String uids,
                                                          @RequestParam(required = false, defaultValue = "false") Boolean all) {
        Map<String, Object> view = new LinkedHashMap<>();
        if (permissionEvictPublisher == null) {
            view.put("ok", false);
            view.put("reason", "MaskPermissionEvictPublisher not available (RedissonClient missing?)");
            return success(view);
        }
        LoginUser u = UserContext.getLoginUser();
        String source = "manual:" + (u == null ? "anonymous" : u.getUid());
        if (Boolean.TRUE.equals(all)) {
            permissionEvictPublisher.publishAll(source);
            view.put("type", "ALL");
        } else if (uids != null && !uids.isEmpty()) {
            Set<String> set = new LinkedHashSet<>(Arrays.asList(uids.split(",")));
            set.removeIf(s -> s == null || s.isEmpty());
            permissionEvictPublisher.publishUser(set, source);
            view.put("type", "USER");
            view.put("uidsCount", set.size());
        } else {
            view.put("ok", false);
            view.put("reason", "param required: uids=u1,u2,... OR all=true");
            return success(view);
        }
        view.put("ok", true);
        view.put("source", source);
        return success(view);
    }
}
