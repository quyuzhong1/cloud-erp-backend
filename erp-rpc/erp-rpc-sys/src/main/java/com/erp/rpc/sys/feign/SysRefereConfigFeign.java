package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * @author jack
 * @Classname SysRefereConfigFeign
 * @Date 2025-05-14
 */

@FeignClient(name = "erp-sys", contextId = "sysRefereConfigFeign",configuration = {FeignErrorDecoder.class})
public interface SysRefereConfigFeign {

    @GetMapping("/feign/sysRefererConfig/getByReferer")
    List<SysRefererConfigEntity> getByReferer(@RequestParam("referer") String referer);

    /**
     * 根据App-Id和应用类型查询配置
     *
     * @param appId   应用ID
     * @param appType 应用类型
     * @return 配置列表
     */
    @GetMapping("/feign/sysRefererConfig/getByAppIdAndType")
    ApiResult<List<SysRefererConfigEntity>> getByAppIdAndType(@RequestParam("appId") String appId, 
                                                             @RequestParam("appType") String appType);

    /**
     * 根据App-Id查询配置
     *
     * @param appId 应用ID
     * @return 配置列表
     */
    @GetMapping("/feign/sysRefererConfig/getByAppId")
    ApiResult<List<SysRefererConfigEntity>> getByAppId(@RequestParam("appId") String appId);
}
