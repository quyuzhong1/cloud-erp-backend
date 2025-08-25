package com.erp.rpc.wms.feign;


import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * wms异步导入feign
 * @author will
 * @date 2025/08/22 17:50
 */
@FeignClient(name = "erp-wms", contextId = "importWmsFeign", configuration = ExportFeignConfig.class)
public interface ImportWmsFeign {

    @PostMapping("/feign/import/sampleRecipient")
    void importSampleRecipient(@RequestBody BaseDTO.ImportDTO dto);

}
