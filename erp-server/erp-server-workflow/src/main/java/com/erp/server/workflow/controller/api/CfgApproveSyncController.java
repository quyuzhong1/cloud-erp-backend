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
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;

/**
 * ERP审批同步配置
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("ERP审批同步配置")
@RequestMapping("/cfgApproveSync")
public class CfgApproveSyncController extends BaseController {

    @Resource
    private CfgApproveSyncService cfgApproveSyncService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "ERP审批同步配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgApproveSyncDTO.AddDTO dto) {
        return success(cfgApproveSyncService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP审批同步配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgApproveSync:update",
        serviceClass = CfgApproveSyncService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgApproveSyncDTO.UpdateDTO dto) {
        cfgApproveSyncService.update(dto);
        return success();
    }



}
