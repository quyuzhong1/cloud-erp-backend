package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.OverseasProviderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "erp-wms", contextId = "overseasProviderFeign")
public interface OverseasProviderFeign {
    /**
     * 根据平台编号查询平台信息
     * @Author Luo_WG
     * @Date 2024/1/18 14:35
     * @param code
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @GetMapping("/feign/overseasProvider/getByPlatformCode")
    OverseasProviderEntity getByPlatformCode(@RequestParam("code") String code);
}
