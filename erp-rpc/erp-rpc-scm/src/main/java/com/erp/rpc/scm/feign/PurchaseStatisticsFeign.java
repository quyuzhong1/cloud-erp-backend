package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.scm.dto.PurchaseStatisticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 采购订单feign
 **/
@FeignClient(name = "erp-scm",contextId = "purchaseStatisticsFeign",configuration = {FeignErrorDecoder.class})
public interface PurchaseStatisticsFeign {

    /**
     * 统计采购订单
     **/
    @PostMapping("feign/purchaseOrder/statisticsBySupplier")
    PurchaseStatisticsDTO.ResponseDTO statisticsBySupplier(@RequestBody PurchaseStatisticsDTO.RequestDTO requestDTO);

    /**
     * 统计执行状态
     **/
    @PostMapping("feign/purchaseOrder/statisticsExecutionStatus")
    PurchaseStatisticsDTO.StatusDTO statisticsExecutionStatus(@RequestBody PurchaseStatisticsDTO.RequestDTO requestDTO);
}
