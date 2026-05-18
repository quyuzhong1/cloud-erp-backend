package com.erp.rpc.sys.feign.mask;

import com.common.business.mask.cache.CfgMaskFieldCacheLoader;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.rpc.sys.feign.CfgMaskFieldFeign;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * cfg_mask_field Redis 缓存 miss 时的 Feign 回源实现。
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class FeignCfgMaskFieldCacheLoader implements CfgMaskFieldCacheLoader {

    @Resource
    private CfgMaskFieldFeign cfgMaskFieldFeign;

    @Override
    public CfgMaskFieldFullCacheDTO load() {
        try {
            ApiResult<CfgMaskFieldFullCacheDTO> result = cfgMaskFieldFeign.listAll();
            return result == null || !result.isSuccess() ? null : result.getData();
        } catch (Throwable e) {
            log.warn("FeignCfgMaskFieldCacheLoader load failed, msg={}", e.getMessage());
            return null;
        }
    }
}
