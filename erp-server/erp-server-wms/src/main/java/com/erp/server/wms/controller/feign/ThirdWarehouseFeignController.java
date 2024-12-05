package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lrp
 */
@RestController
@RequestMapping("feign/thirdWarehouse")
public class ThirdWarehouseFeignController extends BaseController {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    @Resource
    private OverseasProviderService overseasProviderService;
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

    /**
     * 运费试算
     * @param params
     * @return
     */
    @PostMapping("/getCalculateFeeBatch")
    public List<ShippingCalculationDTO.ListDTO> getCalculateFeeBatch(@RequestBody ShippingCalculationDTO.PagingParamDTO params) {
        return overseasProviderService.getCalculateFeeBatch(params);
    }
}
