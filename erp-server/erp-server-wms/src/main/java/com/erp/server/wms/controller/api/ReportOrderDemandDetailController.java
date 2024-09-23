package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.ReportOrderDemandDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;

/**
 * 订单需求明细报表
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@RestController
@LogSystemModule("订单需求明细报表")
@RequestMapping("/reportOrderDemandDetail")
public class ReportOrderDemandDetailController extends BaseController {

    @Resource
    private ReportOrderDemandDetailService reportOrderDemandDetailService;

    /**
    * 新增
    * @author will
    * @date:  2024-09-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "订单需求明细报表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ReportOrderDemandDetailDTO.AddDTO dto) {
        return success(reportOrderDemandDetailService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-09-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "订单需求明细报表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:reportOrderDemandDetail:update",
        serviceClass = ReportOrderDemandDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ReportOrderDemandDetailDTO.UpdateDTO dto) {
        reportOrderDemandDetailService.update(dto);
        return success();
    }



}
