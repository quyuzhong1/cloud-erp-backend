package com.erp.server.auth.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.sys.openapi.CollectorPacksDTO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.sys.openapi.ReturnTrackingDTO;
import com.erp.rpc.oms.feign.CustomerCreditFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.PackingTaskFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
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

    @OpenApi("updateCustomerCredit")
    public ApiResult<String> updateCustomerCredit(@Valid CustomerCreditApplyDTO.UpdateStatusDTO dto) {

        return customerCreditFeign.updateCustomerCredit(dto);
    }

}
