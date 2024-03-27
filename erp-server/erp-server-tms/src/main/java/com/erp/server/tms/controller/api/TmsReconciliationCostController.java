package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.TmsReconciliationCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationCostDTO;

/**
 * 对账费用单
 *
 * @author will
 * @since 2024-03-26
 */
@Slf4j
@RestController
@LogSystemModule("对账费用单")
@RequestMapping("/tmsReconciliationCost")
public class TmsReconciliationCostController extends BaseController {

    @Resource
    private TmsReconciliationCostService tmsReconciliationCostService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "对账费用单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsB2cDeclareReconciliationCostDTO.AddDTO dto) {
        return success(tmsReconciliationCostService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "对账费用单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsB2cDeclareReconciliationCost:update",
        serviceClass = TmsReconciliationCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsB2cDeclareReconciliationCostDTO.UpdateDTO dto) {
        tmsReconciliationCostService.update(dto);
        return success();
    }



}
