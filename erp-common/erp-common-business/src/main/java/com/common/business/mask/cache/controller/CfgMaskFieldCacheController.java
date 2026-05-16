package com.common.business.mask.cache.controller;

import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.core.MaskClassDescriptorRegistry;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

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
}
