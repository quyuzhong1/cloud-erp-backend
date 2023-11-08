package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsOrderOperateLogDTO;

/**
 * 物流平台订单操作记录
 *
 * @author zdy
 * @since 2023-11-08
 */
@Slf4j
@RestController
@LogSystemModule("物流平台订单操作记录")
@RequestMapping("/logisticsOrderOperateLog")
public class LogisticsOrderOperateLogController extends BaseController {

    @Autowired
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    /**
    * 新增
    * @author zdy
    * @date:  2023-11-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流平台订单操作记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsOrderOperateLogDTO.AddDTO dto) {
        return success(logisticsOrderOperateLogService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2023-11-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流平台订单操作记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsOrderOperateLog:update",
        serviceClass = LogisticsOrderOperateLogService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsOrderOperateLogDTO.UpdateDTO dto) {
        logisticsOrderOperateLogService.update(dto);
        return success();
    }



}
