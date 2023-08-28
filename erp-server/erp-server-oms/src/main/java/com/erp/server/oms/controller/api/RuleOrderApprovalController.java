package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleOrderApprovalService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;

/**
 * 订单审核规则
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleOrderApproval")
public class RuleOrderApprovalController extends BaseController {

    @Autowired
    private RuleOrderApprovalService ruleOrderApprovalService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleOrderApprovalDTO.AddDTO dto) {
        return success(ruleOrderApprovalService.add(dto));
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
        menuCode = "oms:ruleOrderApproval:update",
        serviceClass = RuleOrderApprovalService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RuleOrderApprovalDTO.UpdateDTO dto) {
        ruleOrderApprovalService.update(dto);
        return success();
    }



}
