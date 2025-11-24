package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.ShipmentBoxRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ShipmentBoxRuleDTO;

/**
 * 
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/shipmentBoxRule")
public class ShipmentBoxRuleController extends BaseController {

    @Resource
    private ShipmentBoxRuleService shipmentBoxRuleService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShipmentBoxRuleDTO.AddDTO dto) {
        return success(shipmentBoxRuleService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:shipmentBoxRule:update",
        serviceClass = ShipmentBoxRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ShipmentBoxRuleDTO.UpdateDTO dto) {
        shipmentBoxRuleService.update(dto);
        return success();
    }



}
