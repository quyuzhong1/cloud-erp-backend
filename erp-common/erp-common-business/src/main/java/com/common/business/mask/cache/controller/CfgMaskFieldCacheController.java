package com.common.business.mask.cache.controller;

import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.core.MaskClassDescriptorRegistry;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<String> perms = user.getPermissionList();
        view.put("permissionListNull", perms == null);
        view.put("permissionListSize", perms == null ? 0 : perms.size());
        if (probe != null && !probe.isEmpty()) {
            view.put("probe", probe);
            view.put("hasProbe", perms != null && perms.contains(probe));
        }
        if (perms != null && !perms.isEmpty()) {
            view.put("permissionListSample", perms.stream().limit(50).collect(Collectors.toList()));
        }
        return success(view);
    }
}
