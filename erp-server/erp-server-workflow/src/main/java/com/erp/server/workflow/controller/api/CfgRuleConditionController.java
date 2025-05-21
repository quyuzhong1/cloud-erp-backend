package com.erp.server.workflow.controller.api;


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
import com.erp.server.workflow.service.CfgRuleConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgRuleConditionDTO;

/**
 * 规则条件表
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("规则条件表")
@RequestMapping("/cfgRuleCondition")
public class CfgRuleConditionController extends BaseController {

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "规则条件表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleConditionDTO.AddDTO dto) {
        return success(cfgRuleConditionService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "规则条件表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgRuleCondition:update",
        serviceClass = CfgRuleConditionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleConditionDTO.UpdateDTO dto) {
        cfgRuleConditionService.update(dto);
        return success();
    }



}
