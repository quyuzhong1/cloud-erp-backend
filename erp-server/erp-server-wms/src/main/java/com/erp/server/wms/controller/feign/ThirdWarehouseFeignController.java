package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lrp
 */
@RestController
@RequestMapping("feign/thirdWarehouse")
public class ThirdWarehouseFeignController extends BaseController {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @PostMapping("/createOutboundOrder")
    public ApiResult<String> listWarehouseByIds(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(createOutboundReq.getThirdWarehouseProvideCode());
            return service.createOutboundBill(createOutboundReq, createOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }
}
