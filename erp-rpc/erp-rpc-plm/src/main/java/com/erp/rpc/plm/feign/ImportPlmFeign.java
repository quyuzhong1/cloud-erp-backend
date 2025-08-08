package com.erp.rpc.plm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-plm", contextId = "importPlmFeign", configuration = ExportFeignConfig.class)
public interface ImportPlmFeign {

    @PostMapping("/feign/import/productDetailImages")
    void productDetailImages(@RequestBody BaseDTO.ImportDTO dto);

}
