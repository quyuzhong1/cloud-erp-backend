package com.erp.rpc.wms.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "overseasWarehouseFeign")
public interface WmsOverseasWarehouseFeign {

    /**
     * 根据入库状态查询入库单号
     **/
    @PostMapping("/feign/overseasWarehouse/getReceiptNumbersForStatus")
    List<String> getReceiptNumbersForStatus(@RequestBody List<String> statusList);
}
