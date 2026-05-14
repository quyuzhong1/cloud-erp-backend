package com.common.business.cache.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.cache.DorisQuerySettingLocalCache;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

/**
 * 动态数据源 Doris 路由配置本地缓存查询接口
 *
 * <p>放在 {@code erp-common-business} 下，所有依赖该 common 模块且启用动态数据源的业务服务
 * （dmp / tms / wms / oms / fms / srm / scm / plm / workflow）自动暴露此接口；
 * 网关 {@code erp-gateway} 不依赖该 common，自然不会暴露。</p>
 *
 * <p>用途：排查"某个 Pod 本地缓存是否被正确填充 / 版本号是否陈旧"。</p>
 */
@RestController
@RequestMapping("dorisQueryCfgCache")
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingCacheController extends BaseController {

    @Resource
    private DorisQuerySettingLocalCache localCache;

    /**
     * 返回当前节点本地缓存的全量快照
     *
     * <p>响应字段：</p>
     * <ul>
     *   <li>version：快照版本号（System.currentTimeMillis），0 表示未被填充过</li>
     *   <li>size：当前快照内 URI 路由配置条数</li>
     *   <li>lastSyncMillis：本节点最近一次成功 apply 的时间戳，0 表示未 apply 过</li>
     *   <li>data：URI -&gt; DorisQuerySettingDTO 路由明细</li>
     * </ul>
     */
    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        return success(localCache.snapshotView());
    }
}
