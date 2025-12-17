package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.server.oms.service.RuleConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 规则条件表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleCondition")
@LogSystemModule("规则条件表")
public class RuleConditionController extends BaseController {

    @Resource
    private RuleConditionService ruleConditionService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "修改")
    public ApiResult<Object> update(@RequestBody @Validated RuleConditionDTO.UpdateDTO dto) {
        ruleConditionService.update(dto);
        return success();
    }



}
