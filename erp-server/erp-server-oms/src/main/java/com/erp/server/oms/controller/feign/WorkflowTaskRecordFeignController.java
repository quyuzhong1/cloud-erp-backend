package com.erp.server.oms.controller.feign;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务节点记录表
 *
 * @author jack
 * @since 2025-09-16
 */
@Slf4j
@RestController
@RequestMapping("/feign/workflowTaskRecord")
public class WorkflowTaskRecordFeignController extends BaseController {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    /**
     * 查询异常任务汇总 (单据类型 + 节点)维度
     * @author jack
     * @date 2025-09-18
     * @return java.util.List<com.erp.model.oms.dto.WorkflowTaskRecordDTO.TaskErrorReportDTO>
     */
    @PostMapping("/getTaskErrorReport")
    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport(){
        return workflowTaskRecordService.getTaskErrorReport();
    }



}
