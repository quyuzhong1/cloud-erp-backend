package com.erp.rpc.oms.feign;


import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * wms异步导入feign
 * @author jack
 * @date 2025-09-02
 */
@FeignClient(name = "erp-oms", contextId = "importOmsFeign", configuration = ExportFeignConfig.class)
public interface ImportOmsFeign {

    @PostMapping("/feign/import/exhibitionOrder")
    void importExhibitionOrder(@RequestBody BaseDTO.ImportDTO dto);

}
