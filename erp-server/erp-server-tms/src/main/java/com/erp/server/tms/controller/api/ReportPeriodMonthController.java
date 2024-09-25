package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.ReportPeriodMonthService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 核算期间月份表
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("核算期间月份表")
@RequestMapping("/reportPeriodMonth")
public class ReportPeriodMonthController extends BaseController {

    @Resource
    private ReportPeriodMonthService reportPeriodMonthService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "核算期间月份表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ReportPeriodMonthDTO.AddDTO dto) {
        return success(reportPeriodMonthService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "核算期间月份表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:reportPeriodMonth:update",
        serviceClass = ReportPeriodMonthService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ReportPeriodMonthDTO.UpdateDTO dto) {
        reportPeriodMonthService.update(dto);
        return success();
    }

    /**
     * 通过重量分摊获取所有核算期间下拉框
     * @param dto
     * @return
     */
    @PostMapping("/queryList")
    public ApiResult<List<ReportPeriodMonthDTO.SelectDTO>> queryList(@RequestBody @Validated ReportPeriodMonthDTO.QueryDTO dto) {
        return success(reportPeriodMonthService.queryList(dto));
    }
    /**
     * 获取所有核算期间下拉框
     * @return
     */
    @PostMapping("/listLocalDate")
    public ApiResult<List<ReportPeriodMonthDTO.ListDTO>> listLocalDate() {
        return success(reportPeriodMonthService.listLocalDate());
    }
}
