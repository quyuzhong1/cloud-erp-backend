package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;

import javax.validation.Valid;

/**
 * 自发货费用
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@RestController
@LogSystemModule("自发货费用")
@RequestMapping("/logisticsBillCost")
public class LogisticsBillCostController extends BaseController {

    @Autowired
    private LogisticsBillCostService logisticsBillCostService;

    /**
    * 新增
    * @author Will
    * @date:  2023-11-06
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "自发货费用新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsBillCostDTO.AddDTO dto) {
        return success(logisticsBillCostService.add(dto));
    }

    /**
    * 修改
    * @author Will
    * @date:  2023-11-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "自发货费用修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsBillCost:update",
        serviceClass = LogisticsBillCostService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsBillCostDTO.UpdateDTO dto) {
        logisticsBillCostService.update(dto);
        return success();
    }



}
