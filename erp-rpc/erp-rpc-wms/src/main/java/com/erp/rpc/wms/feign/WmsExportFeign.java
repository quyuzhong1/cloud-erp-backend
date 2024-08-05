package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "wmsExportFeign")
public interface WmsExportFeign {

    @PostMapping("/feign/exportAliexpressDelivery")
    List<AliexpressDeliveryDTO.ListDTO> exportAliexpressDeliveryExcel(@RequestBody String metaInfo, int limit, int offset);

    @PostMapping("/feign/exportB2cDeliveryOrder")
    List<SoB2cDeliveryDTO.ListDTO> exportB2cDeliveryOrderExcel(@RequestBody String metaInfo, int limit, int offset);

    int exportAliexpressDeliveryCount(String metaInfo);
}
