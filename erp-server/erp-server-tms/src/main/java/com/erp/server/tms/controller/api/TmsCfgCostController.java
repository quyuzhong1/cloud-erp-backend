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
import com.erp.server.tms.service.TmsCfgCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsCfgCostDTO;

/**
 * 费用管理配置表
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@RestController
@LogSystemModule("费用管理配置表")
@RequestMapping("/tmsCfgCost")
public class TmsCfgCostController extends BaseController {

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "费用管理配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsCfgCostDTO.AddDTO dto) {
        return success(tmsCfgCostService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "费用管理配置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsCfgCost:update",
        serviceClass = TmsCfgCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsCfgCostDTO.UpdateDTO dto) {
        tmsCfgCostService.update(dto);
        return success();
    }



}
