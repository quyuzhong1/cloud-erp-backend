package com.erp.server.workflow.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.WorkflowProcessIdDTO;
import com.erp.model.workflow.dto.FindProcessDTO;
import com.erp.model.workflow.dto.WorkflowBusinessDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.WorkflowBusinessVO;
import com.erp.server.workflow.service.WorkflowBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 旧流程信息

 * @Date 2023-01-30 15:37
 * @Created by yl
 */

@RestController
@RequestMapping("business")
@Slf4j
public class WorkflowBusinessController extends BaseController {

    @Resource
    private WorkflowBusinessService workflowBusinessService;


    /**
     * 获取流程名及相关信息
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<WorkflowBusinessVO>> getProcessList(@RequestBody @Validated FindProcessDTO dto) {
        List<WorkflowBusinessVO> resultList = workflowBusinessService.getBusinessList(dto);
        return success(resultList);
    }


    /**
     * 保存流程信息
     *
     * @return
     */
    @PostMapping("/save")
    public ApiResult<Object> saveProcess(@RequestBody WorkflowBusinessDTO dto) {
        Boolean flag = workflowBusinessService.saveBusiness(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


    /**
     * 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<List<ApproveNodeRecordVO>> auditInfo(@RequestBody @Validated WorkflowProcessIdDTO dto) {
        List<ApproveNodeRecordVO> list=workflowBusinessService.auditInfo(dto.getProcessId());
        return success(list);
    }

}
