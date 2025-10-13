package com.erp.rpc.fms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * fms异步导入feign
 * @author wuht
 * @date 2025/10/13
 */
@FeignClient(name = "erp-fms", contextId = "importFmsFeign", configuration = ExportFeignConfig.class)
public interface ImportFmsFeign {

    @PostMapping("/feign/import/assetLocation")
    void importAssetLocation(@RequestBody BaseDTO.ImportDTO dto);

}

