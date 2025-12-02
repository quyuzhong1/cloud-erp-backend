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
import com.erp.server.oms.service.KolSampleCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolSampleCostDTO;

/**
 * 寄样费用表
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("寄样费用表")
@RequestMapping("/kolSampleCost")
public class KolSampleCostController extends BaseController {

    @Resource
    private KolSampleCostService kolSampleCostService;

    /**
    * 新增
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "寄样费用表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolSampleCostDTO.AddDTO dto) {
        return success(kolSampleCostService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "寄样费用表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolSampleCost:update",
        serviceClass = KolSampleCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolSampleCostDTO.UpdateDTO dto) {
        kolSampleCostService.update(dto);
        return success();
    }



}
