package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.TmsB2cDeclareReconciliationCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationCostDTO;

/**
 * B2c报关单
 *
 * @author will
 * @since 2024-03-26
 */
@Slf4j
@RestController
@LogSystemModule("B2c报关单")
@RequestMapping("/tmsB2cDeclareReconciliationCost")
public class TmsB2cDeclareReconciliationCostController extends BaseController {

    @Resource
    private TmsB2cDeclareReconciliationCostService tmsB2cDeclareReconciliationCostService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2c报关单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsB2cDeclareReconciliationCostDTO.AddDTO dto) {
        return success(tmsB2cDeclareReconciliationCostService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2c报关单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsB2cDeclareReconciliationCost:update",
        serviceClass = TmsB2cDeclareReconciliationCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsB2cDeclareReconciliationCostDTO.UpdateDTO dto) {
        tmsB2cDeclareReconciliationCostService.update(dto);
        return success();
    }



}
