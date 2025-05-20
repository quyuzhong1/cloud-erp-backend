package com.erp.server.workflow.controller.api;


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
import com.erp.server.workflow.service.CfgSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgSettingDTO;

/**
 * 系统配置管理
 *
 * @author jack
 * @since 2025-05-20
 */
@Slf4j
@RestController
@LogSystemModule("系统配置管理")
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingDTO.AddDTO dto) {
        return success(cfgSettingService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "系统配置管理修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgSetting:update",
        serviceClass = CfgSettingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgSettingDTO.UpdateDTO dto) {
        cfgSettingService.update(dto);
        return success();
    }



}
