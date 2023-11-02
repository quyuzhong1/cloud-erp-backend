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
import com.erp.server.tms.service.LogisticsPrintTypeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;

/**
 * 面板打印设置表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("面板打印设置表")
@RequestMapping("/logisticsPrintType")
public class LogisticsPrintTypeController extends BaseController {

    @Autowired
    private LogisticsPrintTypeService logisticsPrintTypeService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "面板打印设置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsPrintTypeDTO.AddDTO dto) {
        return success(logisticsPrintTypeService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsPrintType:update",
        serviceClass = LogisticsPrintTypeService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsPrintTypeDTO.UpdateDTO dto) {
        logisticsPrintTypeService.update(dto);
        return success();
    }



}
