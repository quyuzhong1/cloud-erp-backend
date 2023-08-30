package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfConditionDTO;

/**
 * 条件配置表
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@RestController
@RequestMapping("/cfCondition")
public class CfConditionController extends BaseController {

    @Autowired
    private CfConditionService cfConditionService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated CfConditionDTO.AddDTO dto) {
        return success(cfConditionService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-08-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:cfCondition:update",
        serviceClass = CfConditionService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated CfConditionDTO.UpdateDTO dto) {
        cfConditionService.update(dto);
        return success();
    }



}
