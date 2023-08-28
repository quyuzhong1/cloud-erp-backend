package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleRefConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleRefConditionDTO;

/**
 * 规则关联条件表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleRefCondition")
public class RuleRefConditionController extends BaseController {

    @Autowired
    private RuleRefConditionService ruleRefConditionService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleRefConditionDTO.AddDTO dto) {
        return success(ruleRefConditionService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:ruleRefCondition:update",
        serviceClass = RuleRefConditionService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RuleRefConditionDTO.UpdateDTO dto) {
        ruleRefConditionService.update(dto);
        return success();
    }



}
