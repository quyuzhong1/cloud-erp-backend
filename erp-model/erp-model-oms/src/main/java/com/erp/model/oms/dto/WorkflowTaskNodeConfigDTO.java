package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * dict_basic 任务节点配置（兼容 classPath#method 与 JSON）
 */
@Data
@NoArgsConstructor
public class WorkflowTaskNodeConfigDTO implements Serializable {

    private String serviceCode;

    /**
     * feign_invoke | rpc_feign
     */
    private String handlerType;

    private String classPath;

    private String methodName;

    private Integer timeoutSeconds;

    private Integer maxRetry;

    private Boolean idempotent;
}
