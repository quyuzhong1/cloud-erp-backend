package com.erp.rpc.wms.feign;

import com.erp.model.oms.entity.SoB2cDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 
 * @date 2024-07-02
 * @author tanmujin
 */
@FeignClient(name = "erp-oms", contextId = "soB2cFeign")
public interface SoB2cFeign {

    @PostMapping("/feign/soB2c/listDetailByMainIds")
    List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);
}
