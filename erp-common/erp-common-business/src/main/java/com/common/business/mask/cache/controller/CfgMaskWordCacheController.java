package com.common.business.mask.cache.controller;

import com.common.business.mask.cache.CfgMaskWordLocalCache;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 脱敏词典本地缓存查询接口（运维侧排查用）
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("maskWordCfgCache")
public class CfgMaskWordCacheController extends BaseController {

    @Resource
    private CfgMaskWordLocalCache localCache;

    /**
     * 返回当前节点本地缓存的全量词典快照
     *
     * <p>响应字段：</p>
     * <ul>
     *   <li>version：快照版本号（System.currentTimeMillis），0 表示未被填充过</li>
     *   <li>denySize / allowSize：当前快照内黑/白名单条数</li>
     *   <li>lastSyncMillis：本节点最近一次成功 apply 的时间戳，0 表示未 apply 过</li>
     *   <li>denyWords / allowWords：明细</li>
     * </ul>
     */
    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        return success(localCache.snapshotView());
    }
}
