package com.erp.rpc.wms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author lrp
 */
@FeignClient(name = "erp-wms",contextId = "thirdWarehouse")
public interface ThirdWarehouseFeign {

    /**
     * 创建海外仓出库单
     * @return 出库单号
     */
    @PostMapping("feign/thirdWarehouse/createOutboundOrder")
    ApiResult<String> createOutboundOrder(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq);

    /**
     * 取消海外仓出库单
     */
    @PostMapping("feign/thirdWarehouse/cancelOutboundOrder")
    ApiResult<String> cancelOutboundOrder(@RequestBody ThirdWarehouseCancelOutboundReq cancelOutboundReq);
}
