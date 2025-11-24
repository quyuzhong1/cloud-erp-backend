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
import com.erp.server.oms.service.ShipmentBoxRuleDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ShipmentBoxRuleDetailDTO;

/**
 * 
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/shipmentBoxRuleDetail")
public class ShipmentBoxRuleDetailController extends BaseController {

    @Resource
    private ShipmentBoxRuleDetailService shipmentBoxRuleDetailService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShipmentBoxRuleDetailDTO.AddDTO dto) {
        return success(shipmentBoxRuleDetailService.add(dto));
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
        menuCode = "oms:shipmentBoxRuleDetail:update",
        serviceClass = ShipmentBoxRuleDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ShipmentBoxRuleDetailDTO.UpdateDTO dto) {
        shipmentBoxRuleDetailService.update(dto);
        return success();
    }



}
