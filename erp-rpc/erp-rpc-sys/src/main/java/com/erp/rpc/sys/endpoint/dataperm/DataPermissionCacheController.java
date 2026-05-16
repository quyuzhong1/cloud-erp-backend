package com.erp.rpc.sys.endpoint.dataperm;

import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DataPermissionContextDTO;
import com.erp.rpc.sys.feign.dataperm.FeignDataPermissionContextResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 数据权限本地缓存运维接口
 *
 * <h3>位置说明</h3>
 * <p>本类放在 {@code erp-rpc-sys/.../endpoint/dataperm/}，与同模块下的
 * {@code feign/dataperm/} 平级 —— 明确区分"HTTP 服务端"（本类）和"Feign 客户端契约 + 缓存层"
 * （{@code feign/dataperm/}），不混在 Feign 包里。</p>
 *
 * <p><b>为什么不与 {@code CfgMaskFieldCacheController} 放同一位置（{@code erp-common-business} 下）？</b></p>
 * <ul>
 *   <li>Maven 依赖方向是 {@code erp-rpc-sys → erp-common-business}，反向不允许；</li>
 *   <li>{@code CfgMaskFieldCacheController} 能放 common-business 是因为它注入的 3 个核心对象
 *       （{@code CfgMaskFieldLocalCache} / {@code MaskPermissionResolver} SPI 接口 /
 *       {@code MaskPermissionEvictPublisher}）全部本就在 common-business；</li>
 *   <li>本 controller 的主诊断对象 {@link FeignDataPermissionContextResolver} 在 erp-rpc-sys，
 *       且 Step 1 已评估 dataPerm 永远只有 Feign 一种实现、没抽 SPI 接口，common-business 拿不到这个类。</li>
 * </ul>
 *
 * <p>要把本类"形位对齐"到 common-business，必须二选一：(a) 重抽 SPI 接口，或 (b) 把 evict 改成
 * 只发 publisher 广播、放弃直接查本地缓存（snapshot 失去意义）。两者代价都不小，目前选保留在 erp-rpc-sys
 * 形位偏差但语义直观的方案。</p>
 *
 * <p>部署面：装在 {@code erp-rpc-sys} 后业务侧每个服务（erp-server-scm / oms / wms / ...）启动时
 * 都会自动暴露本接口；每个 Pod 各自一套独立 endpoint，便于定位"具体哪个 Pod 缓存陈旧"。
 * sys 服务本身也会装载，但 sys 不通过 aspect 走 resolver，{@code snapshot} 永远返回 size=0，无害。</p>
 *
 * <h3>路径汇总</h3>
 * <ul>
 *   <li>{@code GET /dataPermCache/snapshot} —— 当前节点 cache 全量视图（uid → 各维度 size）</li>
 *   <li>{@code GET /dataPermCache/whoami} —— 当前请求线程的 LoginUser + 解析出的 ctx 摘要</li>
 *   <li>{@code POST /dataPermCache/evict?uids=u1,u2}（或 {@code ?all=true}）—— 手动失效</li>
 * </ul>
 *
 * <p>失效语义：本接口只在当前 Pod 生效。集群级失效已经由
 * {@link com.erp.rpc.sys.feign.dataperm.DataPermissionContextEvictListener} 自动处理；
 * 这里的手动 evict 通常只用于"绕过 Pub/Sub 临时验证"或"Redis 故障兜底"。</p>
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("dataPermCache")
public class DataPermissionCacheController extends BaseController {

    @Autowired(required = false)
    private FeignDataPermissionContextResolver resolver;

    /**
     * 当前节点 cache 全量视图。
     *
     * <p>响应字段：</p>
     * <ul>
     *   <li>size：当前缓存条目数</li>
     *   <li>ttlMs：单条 TTL</li>
     *   <li>maxCacheSize / depUserLargeWarn：保护阈值</li>
     *   <li>entries：uid → {ageMs, expiredByTtl, 各维度 size}（不暴露明细数据，避免泄露权限码 / 用户 id）</li>
     * </ul>
     */
    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        if (resolver == null) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("enabled", false);
            view.put("reason", "FeignDataPermissionContextResolver not available");
            return success(view);
        }
        return success(resolver.snapshotView());
    }

    /**
     * 排查"权限改了但还看得见"的关键诊断：返回当前线程的 ctx 摘要 + 可选 probe 检查 menuCode 命中情况。
     *
     * <p>使用示例：</p>
     * <pre>GET /dataPermCache/whoami?menuCode=scm:purchaseOrder:paging</pre>
     */
    @GetMapping("/whoami")
    public ApiResult<Map<String, Object>> whoami(@RequestParam(required = false) String menuCode) {
        Map<String, Object> view = new LinkedHashMap<>();
        LoginUser user = UserContext.getLoginUser();
        view.put("loginUserNull", user == null);
        if (user == null || resolver == null) {
            view.put("resolverAvailable", resolver != null);
            return success(view);
        }
        view.put("uid", user.getUid());
        view.put("userName", user.getUserName());

        DataPermissionContextDTO ctx = resolver.resolve(user.getUid());
        view.put("permSize", ctx.getPermissionsList() == null ? 0 : ctx.getPermissionsList().size());
        view.put("roleIdList", ctx.getRoleIdList());
        view.put("depUserSize", ctx.getDepUserList() == null ? 0 : ctx.getDepUserList().size());
        view.put("shopSize", ctx.getShopUserList() == null ? 0 : ctx.getShopUserList().size());
        view.put("warehouseSize", ctx.getWarehouseUserList() == null ? 0 : ctx.getWarehouseUserList().size());

        if (menuCode != null && !menuCode.isEmpty() && ctx.getPermissionsList() != null) {
            boolean hit = ctx.getPermissionsList().stream()
                    .anyMatch(p -> p != null && menuCode.equals(p.getPermissionsCode()));
            view.put("menuCode", menuCode);
            view.put("hasMenuCodeInPermissions", hit);
        }
        return success(view);
    }

    /**
     * 手动失效：精确 uid 集合或整体清空。
     *
     * <p>两个参数二选一；同时传时 {@code all=true} 优先。本接口只在当前 Pod 生效；
     * 集群级失效由 {@link com.erp.rpc.sys.feign.dataperm.DataPermissionContextEvictListener}
     * 自动通过 Pub/Sub 广播处理，正常场景下无需手工调用。</p>
     */
    @PostMapping("/evict")
    public ApiResult<Map<String, Object>> evict(@RequestParam(required = false) String uids,
                                                @RequestParam(required = false, defaultValue = "false") Boolean all) {
        Map<String, Object> view = new LinkedHashMap<>();
        if (resolver == null) {
            view.put("ok", false);
            view.put("reason", "FeignDataPermissionContextResolver not available");
            return success(view);
        }
        if (Boolean.TRUE.equals(all)) {
            resolver.evictAll();
            view.put("type", "ALL");
        } else if (uids != null && !uids.isEmpty()) {
            Set<String> set = new LinkedHashSet<>(Arrays.asList(uids.split(",")));
            set.removeIf(s -> s == null || s.isEmpty());
            resolver.evict(set);
            view.put("type", "USER");
            view.put("uidsCount", set.size());
        } else {
            view.put("ok", false);
            view.put("reason", "param required: uids=u1,u2,... OR all=true");
            return success(view);
        }
        view.put("ok", true);
        return success(view);
    }
}
