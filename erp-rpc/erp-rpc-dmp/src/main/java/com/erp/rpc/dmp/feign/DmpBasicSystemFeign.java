package com.erp.rpc.dmp.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * DMP 基础系统远程调用接口。
 *
 * @author jack
 * @since 2026-06-29
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmp/basicSystem", contextId = "dmpBasicSystemFeign", configuration = {FeignErrorDecoder.class})
public interface DmpBasicSystemFeign {

    /**
     * 根据 ID 查询基础系统。
     *
     * @param id 基础系统 ID
     * @return 基础系统
     */
    @GetMapping("/getById")
    DmpBasicSystemEntity getById(@RequestParam("id") String id);
}
