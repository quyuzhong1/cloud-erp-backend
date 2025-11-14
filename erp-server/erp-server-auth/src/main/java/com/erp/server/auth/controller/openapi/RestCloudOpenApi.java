package com.erp.server.auth.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.rpc.oms.feign.CustomerCreditFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.auth.config.OpenApi;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 对接汇创智能设备
 * </p>
 */
@OpenApi
public class RestCloudOpenApi {

    @Resource
    private CustomerCreditFeign customerCreditFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @OpenApi("updateCustomerCredit")
    public ApiResult<String> updateCustomerCredit(@Valid CustomerCreditApplyDTO.UpdateStatusDTO dto) {

        return customerCreditFeign.updateCustomerCredit(dto);
    }

    /**
     * 更新销售订单平台订单ID
     * @author will
     * @date 2025/9/23 11:44
     * @param dto
     * @return ApiResult<String>
     */
    @OpenApi("updateDhfPlatformOrderId")
    public ApiResult<Boolean> updateDhfPlatformOrderId(@Valid SoInfoDTO.UpdatePlatformOrderIdDTO dto) {
        return ApiResult.success(soInfoFeign.updateDhfPlatformOrderId(dto));
    }

}
