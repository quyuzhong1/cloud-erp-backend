package com.erp.server.oms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.rpc.oms.feign.WorkflowTaskNodeFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨服务任务编排节点 Feign 契约示例
 */
@Slf4j
@RestController
@RequestMapping("/feign/workflowTaskNode")
public class WorkflowTaskNodeFeignController extends BaseController {

    @PostMapping("/contractEcho")
    public ApiResult<WorkflowTaskRecordDTO.MqResponseDTO> contractEcho(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO response = new WorkflowTaskRecordDTO.MqResponseDTO();
        response.setData(dto.getData());
        return success(response);
    }
}
