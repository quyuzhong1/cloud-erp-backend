package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.rpc.oms.feign.CustomerCreditFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.tms.feign.ImprotHistoryRecordFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

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
    @Resource
    private ImprotHistoryRecordFeign improtHistoryRecordFeign;

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

    /**
     * 导入的Excel数据（预处理、导入、导入确认）
     * @author will
     * @date 2026/1/20 18:43
     * @param dto
     * @return ApiResult<Object>
     */
    @OpenApi("/preprocessingImportExcel")
    public ApiResult<List<BatchResultDTO>>  preprocessingImportExcel(@RequestBody @Validated ImportHistoryRecordDTO.ImportDTO dto) {
        return ApiResult.success(improtHistoryRecordFeign.preprocessingImportExcel(dto));
    }

}
