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
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;

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

    @GetMapping("/WorkflowTaskRecordRetryJob")
    public void WorkflowTaskRecordRetryJob(@RequestParam("id") String id) {
        workflowTaskRecordService.WorkflowTaskRecordRetryJob(id);
    }



}
