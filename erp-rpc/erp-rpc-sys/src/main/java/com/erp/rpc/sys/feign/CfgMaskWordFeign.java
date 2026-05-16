package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.core.controller.vo.ApiResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 脱敏词典 Feign 契约
 *
 * <p>提供给业务节点冷启动兜底拉取（在 Redis Bucket 不可用 / 内容缺失时使用）。
 * 正常运行期，业务节点优先靠 Redis Pub/Sub 推送 + Bucket 持久化。</p>
 *
 * @author cloud-erp
 */
@FeignClient(name = "erp-sys", contextId = "cfgMaskWordFeign", configuration = {FeignErrorDecoder.class})
public interface CfgMaskWordFeign {

    /**
     * 拉取全量启用词典（不含已删除/已禁用）
     */
    @GetMapping("/feign/cfgMaskWord/listAll")
    ApiResult<CfgMaskWordFullCacheDTO> listAll();

    /**
     * 触发一次广播：按当前表数据重新生成 FullCache 写入 Bucket 并 publish
     */
    @PostMapping("/feign/cfgMaskWord/refresh")
    ApiResult<Boolean> refresh();
}
