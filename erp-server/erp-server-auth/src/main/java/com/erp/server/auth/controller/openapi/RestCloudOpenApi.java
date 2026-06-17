package com.erp.server.auth.controller.openapi;

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
import org.springframework.web.bind.annotation.RequestBody;
import javax.annotation.Resource;
import javax.validation.Valid;
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
     * 开放接口：物流 Excel 导入（已切换为物流商对账单导入，入参契约保持不变）。
     * <p>processingType：preprocessing / import → 仅落库待确认；confirmImport → 落库并已确认。</p>
     *
     * @author will
     * @date 2026/1/20 18:43
     * @param dto 业务类型、费用类型、对账月份、文件列表及处理类型
     * @return 各文件异步导入任务结果
     */
    @OpenApi("preprocessingImportExcel")
    public ApiResult<List<BatchResultDTO>>  preprocessingImportExcel(@Valid ImportHistoryRecordDTO.ImportDTO dto) {
        return improtHistoryRecordFeign.preprocessingImportExcel(dto);
    }

}
