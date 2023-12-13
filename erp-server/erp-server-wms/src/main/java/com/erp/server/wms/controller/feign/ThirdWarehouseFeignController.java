package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author lrp
 */
@RestController
@RequestMapping("feign/thirdWarehouse")
public class ThirdWarehouseFeignController extends BaseController {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @PostMapping("/createOutboundOrder")
    public ApiResult<String> createOutboundOrder(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(createOutboundReq.getThirdWarehouseProvideCode());
            return service.createOutboundBill(createOutboundReq, createOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }


    @PostMapping("/cancelOutboundOrder")
    public ApiResult<String> cancelOutboundOrder(@RequestBody ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(cancelOutboundReq.getThirdWarehouseProvideCode());
            return service.cancelOutboundBill(cancelOutboundReq, cancelOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }
}
