package com.erp.server.sys.controller.feign;

import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.sys.service.CfgMaskFieldService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 字段脱敏配置 Feign 控制器
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("/feign/cfgMaskField")
public class CfgMaskFieldFeignController extends BaseController {

    @Resource
    private CfgMaskFieldService cfgMaskFieldService;

    /**
     * 拉取全量未禁用的字段脱敏配置
     */
    @GetMapping("/listAll")
    public ApiResult<CfgMaskFieldFullCacheDTO> listAll() {
        return success(cfgMaskFieldService.listAllForCache());
    }

    /**
     * 刷新 Redis 缓存
     */
    @PostMapping("/refresh")
    public ApiResult<Boolean> refresh() {
        return success(cfgMaskFieldService.publishFullCache());
    }
}
