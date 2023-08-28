package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleLogisticsService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleLogisticsDTO;

/**
 * 物流规则表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleLogistics")
public class RuleLogisticsController extends BaseController {

    @Autowired
    private RuleLogisticsService ruleLogisticsService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleLogisticsDTO.AddDTO dto) {
        return success(ruleLogisticsService.add(dto));
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
        menuCode = "oms:ruleLogistics:update",
        serviceClass = RuleLogisticsService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RuleLogisticsDTO.UpdateDTO dto) {
        ruleLogisticsService.update(dto);
        return success();
    }



}
