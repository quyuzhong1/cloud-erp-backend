package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleConditionDTO;

/**
 * 规则条件表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleCondition")
public class RuleConditionController extends BaseController {

    @Autowired
    private RuleConditionService ruleConditionService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleConditionDTO.AddDTO dto) {
        return success(ruleConditionService.add(dto));
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
        menuCode = "oms:ruleCondition:update",
        serviceClass = RuleConditionService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RuleConditionDTO.UpdateDTO dto) {
        ruleConditionService.update(dto);
        return success();
    }



}
