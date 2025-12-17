package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "workflowTaskRecordFeign",configuration = {FeignErrorDecoder.class})
public interface WorkflowTaskRecordFeign {
    /**
     * 查询异常任务汇总 (单据类型 + 节点)维度
     * @author jack
     * @date 2025-09-18
     * @return java.util.List<com.erp.model.oms.dto.WorkflowTaskRecordDTO.TaskErrorReportDTO>
     */
    @PostMapping("/feign/workflowTaskRecord/getTaskErrorReport")
    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport();

}
