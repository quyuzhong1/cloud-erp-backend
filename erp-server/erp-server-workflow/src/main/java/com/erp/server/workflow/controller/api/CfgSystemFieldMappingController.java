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
import com.erp.server.workflow.service.CfgSystemFieldMappingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgSystemFieldMappingDTO;

/**
 * 远程查询配置
 *
 * @author will
 * @since 2025-10-17
 */
@Slf4j
@RestController
@LogSystemModule("远程查询配置")
@RequestMapping("/cfgSystemFieldMapping")
public class CfgSystemFieldMappingController extends BaseController {

    @Resource
    private CfgSystemFieldMappingService cfgSystemFieldMappingService;

    /**
    * 新增
    * @author will
    * @date:  2025-10-17
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "远程查询配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSystemFieldMappingDTO.AddDTO dto) {
        return success(cfgSystemFieldMappingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-10-17
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "远程查询配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgSystemFieldMapping:update",
        serviceClass = CfgSystemFieldMappingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgSystemFieldMappingDTO.UpdateDTO dto) {
        cfgSystemFieldMappingService.update(dto);
        return success();
    }



}
