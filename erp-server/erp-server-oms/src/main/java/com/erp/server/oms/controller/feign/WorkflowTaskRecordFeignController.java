package com.erp.server.oms.controller.feign;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.controller.BaseController;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.service.WorkflowTaskInstanceService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Resource
    private WorkflowTaskInstanceService workflowTaskInstanceService;

    /**
     * 查询异常任务汇总 (单据类型 + 节点)维度
     *
     * @author jack
     * @date 2025-09-18
     * @return java.util.List<com.erp.model.oms.dto.WorkflowTaskRecordDTO.TaskErrorReportDTO>
     */
    @PostMapping("/getTaskErrorReport")
    public List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport() {
        return workflowTaskRecordService.getTaskErrorReport();
    }

    /**
     * 启动组包预报自动出库任务编排（同步返回受理结果与 instanceId）。
     */
    @PostMapping("/startMergePackageDeliveryWorkflow")
    public WorkflowTaskRecordDTO.StartWorkflowResultDTO startMergePackageDeliveryWorkflow(@RequestBody WorkflowTaskRecordDTO.StartWorkflowDTO dto) {
        if (dto == null || CharSequenceUtil.isBlank(dto.getSourceId())) {
            throw new ServiceException("sourceId不能为空");
        }
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
        addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.SO_B2C_MERGE_PACKAGE_DELIVERY);
        addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
        addTaskDTO.setSourceId(dto.getSourceId());
        addTaskDTO.setSourceCode(CharSequenceUtil.blankToDefault(dto.getSourceCode(), dto.getSourceId()));
        addTaskDTO.setTraceId(CharSequenceUtil.blankToDefault(dto.getTraceId(), TraceContext.traceId()));
        addTaskDTO.setFirstNodeInputData(buildFirstNodeInputData(dto));
        workflowTaskRecordService.startOrResume(addTaskDTO);

        WorkflowTaskInstanceEntity latest = null;
        if (CharSequenceUtil.isNotBlank(addTaskDTO.getInstanceId())) {
            latest = workflowTaskInstanceService.getById(addTaskDTO.getInstanceId());
        }
        if (latest == null) {
            latest = workflowTaskInstanceService.getLatestBySource(
                    dto.getSourceId(), WorkflowTaskRecordTypeEnum.SO_B2C_MERGE_PACKAGE_DELIVERY.getCode());
        }
        WorkflowTaskRecordDTO.StartWorkflowResultDTO resultDTO = new WorkflowTaskRecordDTO.StartWorkflowResultDTO();
        resultDTO.setAccepted(Boolean.TRUE);
        if (latest != null) {
            resultDTO.setInstanceId(latest.getId());
            resultDTO.setSourceId(latest.getSourceId());
            resultDTO.setSourceCode(latest.getSourceCode());
            resultDTO.setTraceId(latest.getTraceId());
            resultDTO.setMessage("任务已受理");
        } else {
            // 兜底：调度已受理，但实例查询短暂不可见时不抛错，避免前端误判失败。
            resultDTO.setInstanceId(addTaskDTO.getInstanceId());
            resultDTO.setSourceId(dto.getSourceId());
            resultDTO.setSourceCode(CharSequenceUtil.blankToDefault(dto.getSourceCode(), dto.getSourceId()));
            resultDTO.setTraceId(addTaskDTO.getTraceId());
            resultDTO.setMessage("任务已受理，实例信息同步中");
            log.warn("组包预报自动出库任务已受理但未立即查到实例，sourceId={}, sourceType={}",
                    dto.getSourceId(), WorkflowTaskRecordTypeEnum.SO_B2C_MERGE_PACKAGE_DELIVERY.getCode());
        }
        return resultDTO;
    }

    private Map<String, Object> buildFirstNodeInputData(WorkflowTaskRecordDTO.StartWorkflowDTO dto) {
        Map<String, Object> map = new HashMap<>();
        String sourceId = dto == null ? "" : dto.getSourceId();
        String sourceCode = dto == null ? "" : dto.getSourceCode();
        if (dto != null && dto.getFirstNodeInputData() != null) {
            map.putAll(dto.getFirstNodeInputData());
        }
        map.putIfAbsent("soId", sourceId);
        map.putIfAbsent("id", sourceId);
        map.putIfAbsent("sourceCode", sourceCode);
        return map;
    }
}
