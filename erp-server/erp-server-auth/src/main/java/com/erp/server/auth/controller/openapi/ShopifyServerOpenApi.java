package com.erp.server.auth.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ShopifyServerSoB2cDTO;
import com.erp.rpc.oms.feign.SoB2cForeignFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@OpenApi
@Component
public class ShopifyServerOpenApi {

    @Resource
    private SoB2cForeignFeign soB2cForeignFeign;

    /**
     * 获取Shopify订单物流信息
     */
    @OpenApi("getB2cOrderLogisticInfo")
    public ApiResult<List<ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO>> getB2cOrderLogisticInfo(@Valid ShopifyServerSoB2cDTO.SoB2cLogisticQueryDTO dto) {
        List<ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO> result = soB2cForeignFeign.getShopifyLogisticInfo(dto);
        return ApiResult.success(result);
    }
}
