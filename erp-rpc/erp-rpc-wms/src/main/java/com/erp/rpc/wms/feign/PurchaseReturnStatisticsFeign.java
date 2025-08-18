package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "erp-wms", contextId = "purchaseReturnStatisticsFeign" ,configuration = {FeignErrorDecoder.class})
public interface PurchaseReturnStatisticsFeign {

    @PostMapping("feign/purchaseReturnStatistics/statisticsBySupplier")
    PurchaseReturnStatisticsDTO.ResponseDTO statisticsBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO);


    @PostMapping("feign/purchaseReturnStatistics/confirmStatusCountBySupplier")
    PurchaseReturnStatisticsDTO.StatusDTO confirmStatusCountBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO);
}