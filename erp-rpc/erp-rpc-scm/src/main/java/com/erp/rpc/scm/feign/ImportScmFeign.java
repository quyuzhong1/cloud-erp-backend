package com.erp.rpc.scm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-scm", contextId = "importScmFeign", configuration = ExportFeignConfig.class)
public interface ImportScmFeign {

    @PostMapping("/feign/import/importAssetNotice")
    void importAssetNotice(@RequestBody BaseDTO.ImportDTO dto);

    @PostMapping("/feign/import/importAssetPurchaseOrder")
    void importAssetPurchaseOrder(@RequestBody BaseDTO.ImportTypeDTO dto);

}
