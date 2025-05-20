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
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;

/**
 * process_task_management拓展表
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("process_task_management拓展表")
@RequestMapping("/processTaskManagementExt")
public class ProcessTaskManagementExtController extends BaseController {

    @Resource
    private ProcessTaskManagementExtService processTaskManagementExtService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "process_task_management拓展表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ProcessTaskManagementExtDTO.AddDTO dto) {
        return success(processTaskManagementExtService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "process_task_management拓展表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:processTaskManagementExt:update",
        serviceClass = ProcessTaskManagementExtService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ProcessTaskManagementExtDTO.UpdateDTO dto) {
        processTaskManagementExtService.update(dto);
        return success();
    }



}
