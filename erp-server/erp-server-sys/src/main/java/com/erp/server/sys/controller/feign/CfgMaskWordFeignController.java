package com.erp.server.sys.controller.feign;

import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.sys.service.CfgMaskWordService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 脱敏词典 Feign 控制器
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("/feign/cfgMaskWord")
public class CfgMaskWordFeignController extends BaseController {

    @Resource
    private CfgMaskWordService cfgMaskWordService;

    /**
     * 拉取全量未禁用的脱敏词典
     */
    @GetMapping("/listAll")
    public ApiResult<CfgMaskWordFullCacheDTO> listAll() {
        return success(cfgMaskWordService.listAllForCache());
    }

    /**
     * 刷新 Redis 缓存
     */
    @PostMapping("/refresh")
    public ApiResult<Boolean> refresh() {
        return success(cfgMaskWordService.publishFullCache());
    }
}
