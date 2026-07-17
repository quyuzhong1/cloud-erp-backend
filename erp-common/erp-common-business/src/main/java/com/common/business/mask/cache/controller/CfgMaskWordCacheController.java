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
 * 脱敏词典 Redis 缓存查询接口（运维侧排查用）
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("maskWordCfgCache")
public class CfgMaskWordCacheController extends BaseController {

    @Resource
    private CfgMaskWordLocalCache localCache;

    /**
     * 返回 Redis 词典缓存和当前 SensitiveWordBs 引擎镜像
     *
     * <p>响应字段：</p>
     * <ul>
     *   <li>redisVersion：Redis 缓存版本号</li>
     *   <li>denySize / allowSize：当前引擎镜像内黑/白名单条数</li>
     *   <li>lastSyncMillis：本节点最近一次同步引擎的时间戳，0 表示未同步过</li>
     *   <li>denyWords / allowWords：明细</li>
     * </ul>
     */
    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        return success(localCache.snapshotView());
    }
}
