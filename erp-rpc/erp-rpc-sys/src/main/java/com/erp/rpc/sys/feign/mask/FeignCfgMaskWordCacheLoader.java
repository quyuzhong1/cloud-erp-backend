package com.erp.rpc.sys.feign.mask;

import com.common.business.mask.cache.CfgMaskWordCacheLoader;
import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.rpc.sys.feign.CfgMaskWordFeign;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * cfg_mask_word Redis 缓存 miss 时的 Feign 回源实现。
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class FeignCfgMaskWordCacheLoader implements CfgMaskWordCacheLoader {

    @Resource
    private CfgMaskWordFeign cfgMaskWordFeign;

    @Override
    public CfgMaskWordFullCacheDTO load() {
        try {
            ApiResult<CfgMaskWordFullCacheDTO> result = cfgMaskWordFeign.listAll();
            return result == null || !result.isSuccess() ? null : result.getData();
        } catch (Throwable e) {
            log.warn("FeignCfgMaskWordCacheLoader load failed, msg={}", e.getMessage());
            return null;
        }
    }
}
