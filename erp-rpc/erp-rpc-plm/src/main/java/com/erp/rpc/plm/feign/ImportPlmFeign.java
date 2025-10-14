package com.erp.rpc.plm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-plm", contextId = "importPlmFeign", configuration = ExportFeignConfig.class)
public interface ImportPlmFeign {

    @PostMapping("/feign/import/productDetailImages")
    void productDetailImages(@RequestBody BaseDTO.ImportDTO dto);


    @PostMapping("/feign/import/skuStdCostDetail")
    void skuStdCostDetail(@RequestBody BaseDTO.ImportTypeDTO dto);

    @PostMapping("/feign/import/importMoldInfo")
    void importMoldInfo(@RequestBody BaseDTO.ImportTypeDTO dto);

    @PostMapping("/feign/import/importMoldRefSku")
    void importMoldRefSku(@RequestBody BaseDTO.ImportTypeDTO dto);

}
