package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleDeliveryWarehouseService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;

/**
 * 发货仓库规则表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleDeliveryWarehouse")
public class RuleDeliveryWarehouseController extends BaseController {

    @Autowired
    private RuleDeliveryWarehouseService ruleDeliveryWarehouseService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleDeliveryWarehouseDTO.AddDTO dto) {
        return success(ruleDeliveryWarehouseService.add(dto));
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
        menuCode = "oms:ruleDeliveryWarehouse:update",
        serviceClass = RuleDeliveryWarehouseService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RuleDeliveryWarehouseDTO.UpdateDTO dto) {
        ruleDeliveryWarehouseService.update(dto);
        return success();
    }



}
