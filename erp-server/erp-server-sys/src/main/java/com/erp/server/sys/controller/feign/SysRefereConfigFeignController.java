package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.server.sys.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname SysRefereConfigFeignController
 * @Date 2025-05-14
 * @Created by jack
 */
@RestController
@RequestMapping("feign/sysRefererConfig")
@Slf4j
public class SysRefereConfigFeignController extends BaseController {

    @Resource
    private SysRefererConfigService sysRefererConfigService;

    @GetMapping("/getByReferer")
    public List<SysRefererConfigEntity> getByReferer(@RequestParam("referer") String referer) {
        return sysRefererConfigService.lambdaQuery().eq(SysRefererConfigEntity::getAppId, referer).list();
    }

    /**
     * 根据App-Id和应用类型查询配置
     *
     * @param appId   应用ID
     * @param appType 应用类型
     * @return 配置列表
     */
    @GetMapping("/getByAppIdAndType")
    public ApiResult<List<SysRefererConfigEntity>> getByAppIdAndType(@RequestParam("appId") String appId,
                                                                    @RequestParam("appType") String appType) {
        try {
            log.info("查询应用配置，appId：{}，appType：{}", appId, appType);
            
            List<SysRefererConfigEntity> configList = sysRefererConfigService.getByAppIdAndType(appId, appType);
            
            return ApiResult.success(configList);
        } catch (Exception e) {
            log.error("查询应用配置失败，appId：{}，appType：{}", appId, appType, e);
            return ApiResult.error(500, "查询应用配置失败：" + e.getMessage());
        }
    }

    /**
     * 根据App-Id查询配置
     *
     * @param appId 应用ID
     * @return 配置列表
     */
    @GetMapping("/getByAppId")
    public ApiResult<List<SysRefererConfigEntity>> getByAppId(@RequestParam("appId") String appId) {
        try {
            log.info("查询应用配置，appId：{}", appId);
            
            List<SysRefererConfigEntity> configList = sysRefererConfigService.getByAppId(appId);
            
            return ApiResult.success(configList);
        } catch (Exception e) {
            log.error("查询应用配置失败，appId：{}", appId, e);
            return ApiResult.error(500, "查询应用配置失败：" + e.getMessage());
        }
    }
}
