package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 跨服务任务编排节点契约。
 * <p>
 * 各域实现相同签名的节点方法：{@code MqResponseDTO method(MqRequestDTO dto)}，
 * 短期通过 OMS {@code FeignQuery.invoke(classPath, methodName, dto)} 调用；
 * 中期可在各域 {@code erp-rpc-*} 声明域内 Feign 并在 dict 中配置 {@code handlerType=rpc_feign}。
 * </p>
 */
@FeignClient(name = "erp-oms", contextId = "workflowTaskNodeFeign", configuration = {FeignErrorDecoder.class})
public interface WorkflowTaskNodeFeign {

    /**
     * 契约示例：节点方法统一入参/出参（实际节点分布在各域 Controller/Feign 实现类）。
     */
    @PostMapping("/feign/workflowTaskNode/contractEcho")
    ApiResult<WorkflowTaskRecordDTO.MqResponseDTO> contractEcho(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto);
}
