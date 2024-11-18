package com.erp.server.auth.controller.openapi;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import com.erp.rpc.oms.feign.SoB2cForeignFeign;
import com.erp.server.auth.config.OpenApi;

@OpenApi
@Component
public class SoB2cOpenApi {

    @Resource
    private SoB2cForeignFeign soB2cForeignFeign;

    @OpenApi("getB2cOrderDeliveryInfo")
    public ApiResult<PagingVO<SoB2cForeignDTO.OrderDeliveryResp>> getB2cOrderDeliveryInfo(@Valid PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> orderDeliveryReq) {
        PagingVO<SoB2cForeignDTO.OrderDeliveryResp> result = soB2cForeignFeign.getOrderDeliveryInfo(orderDeliveryReq);
        return ApiResult.success(result);
    }
}
