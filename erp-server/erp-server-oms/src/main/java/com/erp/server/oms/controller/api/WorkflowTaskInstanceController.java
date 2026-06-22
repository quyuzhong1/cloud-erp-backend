package com.erp.server.oms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.WorkflowTaskInstanceDTO;
import com.erp.server.oms.query.WorkflowTaskInstanceQueryHandler;
import com.erp.server.oms.service.WorkflowTaskInstanceService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务编排实例监控
 */
@Slf4j
@RestController
@LogSystemModule("任务编排实例")
@RequestMapping("/workflowTaskInstance")
public class WorkflowTaskInstanceController extends BaseController {

    @Resource
    private WorkflowTaskInstanceService workflowTaskInstanceService;

    /**
     * 实例分页：支持高级搜索、数据权限与进度/错误摘要展示。
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskInstance:paging",
            tableAlias = "wti")
    @WebAdvanceQuery(handler = WorkflowTaskInstanceQueryHandler.class)
    public ApiResult<PagingVO<WorkflowTaskInstanceDTO.ListDTO>> paging(
            @RequestBody @Validated PagingDTO<WorkflowTaskInstanceDTO.PagingParamDTO> dto) {
        return success(workflowTaskInstanceService.paging(dto));
    }

    /**
     * 实例详情：含节点时间线、Feign 耗时、目标服务及 autoRetryExceeded 标记。
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskInstance:view",
            serviceClass = WorkflowTaskInstanceService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<WorkflowTaskInstanceDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(workflowTaskInstanceService.view(id));
    }

    /**
     * 按业务单查询全部编排实例历史（同一 sourceId 可能有多轮触发）。
     */
    @GetMapping("/listBySource")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskInstance:view",
            tableAlias = "wti")
    public ApiResult<List<WorkflowTaskInstanceDTO.ViewDTO>> listBySource(
            @Validated @ModelAttribute WorkflowTaskInstanceDTO.ListBySourceParamDTO param) {
        return success(workflowTaskInstanceService.listBySource(param));
    }

    /**
     * 异常统计报表：按业务类型、节点、目标服务聚合失败次数。
     */
    @PostMapping("/errorReport")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskInstance:paging",
            tableAlias = "wti")
    public ApiResult<List<WorkflowTaskInstanceDTO.ErrorReportDTO>> errorReport(
            @RequestBody @Validated WorkflowTaskInstanceDTO.ErrorReportParamDTO param) {
        return success(workflowTaskInstanceService.errorReport(param));
    }

    /**
     * 人工重试失败节点：可指定 stepId 或 instanceId，委托 forceRetry 并返回调度结果。
     */
    @PostMapping("/retry")
    @LogAction(value = LogActionEnum.EXECUTE, desc = "任务编排实例重试")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskRecord:forceRetry",
            serviceClass = WorkflowTaskRecordService.class,
            keyIdName = "stepId")
    public ApiResult<WorkflowTaskInstanceDTO.RetryResultDTO> retry(
            @RequestBody @Validated WorkflowTaskInstanceDTO.RetryDTO dto) {
        return success(workflowTaskInstanceService.retry(dto));
    }

    /**
     * 从指定 index 起重置后续节点并重新调度（用于从中间步骤整段重跑）。
     */
    @PostMapping("/retryFromStep")
    @LogAction(value = LogActionEnum.EXECUTE, desc = "任务编排从指定节点重试")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskRecord:forceRetry",
            serviceClass = WorkflowTaskInstanceService.class,
            keyIdName = "instanceId")
    public ApiResult<WorkflowTaskInstanceDTO.RetryResultDTO> retryFromStep(
            @RequestBody @Validated WorkflowTaskInstanceDTO.RetryFromStepDTO dto) {
        return success(workflowTaskInstanceService.retryFromStep(dto));
    }

    /**
     * 取消运行中的编排实例，未成功节点一并终止。
     */
    @PostMapping("/cancel")
    @LogAction(value = LogActionEnum.EXECUTE, desc = "取消任务编排实例")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:workflowTaskInstance:cancel",
            serviceClass = WorkflowTaskInstanceService.class,
            keyIdName = "instanceId")
    public ApiResult<Void> cancel(@RequestBody @Validated WorkflowTaskInstanceDTO.CancelDTO dto) {
        workflowTaskInstanceService.cancel(dto);
        return success();
    }
}
