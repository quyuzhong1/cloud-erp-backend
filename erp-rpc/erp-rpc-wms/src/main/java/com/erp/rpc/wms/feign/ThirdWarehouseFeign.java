package com.erp.rpc.wms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author lrp
 */
@FeignClient(name = "erp-wms",contextId = "thirdWarehouse")
public interface ThirdWarehouseFeign {

    /**
     * 创建海外仓出库单
     */
    @PostMapping("feign/thirdWarehouse/createOutboundOrder")
    ApiResult<String> createOutboundOrder(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq);

}
