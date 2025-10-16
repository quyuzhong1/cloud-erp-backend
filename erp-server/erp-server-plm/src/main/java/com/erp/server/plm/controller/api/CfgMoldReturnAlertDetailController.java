package com.erp.server.plm.controller.api;


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
import com.erp.server.plm.service.CfgMoldReturnAlertDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.CfgMoldReturnAlertDetailDTO;

/**
 * 模具返回策略明细
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@RestController
@LogSystemModule("模具返回策略明细")
@RequestMapping("/cfgMoldReturnAlertDetail")
public class CfgMoldReturnAlertDetailController extends BaseController {

    @Resource
    private CfgMoldReturnAlertDetailService cfgMoldReturnAlertDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具返回策略明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgMoldReturnAlertDetailDTO.AddDTO dto) {
        return success(cfgMoldReturnAlertDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具返回策略明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:cfgMoldReturnAlertDetail:update",
        serviceClass = CfgMoldReturnAlertDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgMoldReturnAlertDetailDTO.UpdateDTO dto) {
        cfgMoldReturnAlertDetailService.update(dto);
        return success();
    }



}
