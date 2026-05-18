package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.core.controller.vo.ApiResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 字段脱敏配置 Feign 契约
 *
 * <p>提供给业务节点 Redis cache-aside 回源使用（在 Redis Bucket 内容缺失时使用）。</p>
 *
 * @author cloud-erp
 */
@FeignClient(name = "erp-sys", contextId = "cfgMaskFieldFeign", configuration = {FeignErrorDecoder.class})
public interface CfgMaskFieldFeign {

    /**
     * 拉取全量未禁用的字段脱敏配置（不含已删除/已禁用）
     */
    @GetMapping("/feign/cfgMaskField/listAll")
    ApiResult<CfgMaskFieldFullCacheDTO> listAll();

    /**
     * 按当前表数据重新生成 FullCache 写入 Redis
     * （供运维侧"强制刷新 Redis 缓存"使用）
     */
    @PostMapping("/feign/cfgMaskField/refresh")
    ApiResult<Boolean> refresh();
}
