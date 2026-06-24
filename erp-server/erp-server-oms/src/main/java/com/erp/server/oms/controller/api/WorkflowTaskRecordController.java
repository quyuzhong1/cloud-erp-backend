package com.erp.server.oms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 任务节点记录表
 *
 * @author jack
 * @since 2025-09-16
 */
@Slf4j
@RestController
@LogSystemModule("任务节点记录表")
@RequestMapping("/workflowTaskRecord")
public class WorkflowTaskRecordController extends BaseController {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @PostMapping("/forceRetry")
    @LogAction(value = LogActionEnum.EXECUTE, desc = "任务节点人工强制重试")
    public ApiResult<WorkflowTaskRecordDTO.ForceRetryResultDTO> forceRetry(@RequestBody @Validated WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        return success(workflowTaskRecordService.forceRetry(dto));
    }

}
