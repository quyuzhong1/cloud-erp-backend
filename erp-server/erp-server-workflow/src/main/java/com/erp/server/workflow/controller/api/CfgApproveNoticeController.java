package com.erp.server.workflow.controller.api;


import com.erp.server.workflow.service.CfgThirdProcessService;
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
import com.erp.server.workflow.service.CfgApproveNoticeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgApproveNoticeDTO;

/**
 * ERP审批同步-通知配置
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("ERP审批同步-通知配置")
@RequestMapping("/cfgApproveNotice")
public class CfgApproveNoticeController extends BaseController {

    @Resource
    private CfgApproveNoticeService cfgApproveNoticeService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "ERP审批同步-通知配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgApproveNoticeDTO.AddDTO dto) {
        return success(cfgApproveNoticeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP审批同步-通知配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgApproveNotice:update",
        serviceClass = CfgApproveNoticeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgApproveNoticeDTO.UpdateDTO dto) {
        cfgApproveNoticeService.update(dto);
        return success();
    }



}
