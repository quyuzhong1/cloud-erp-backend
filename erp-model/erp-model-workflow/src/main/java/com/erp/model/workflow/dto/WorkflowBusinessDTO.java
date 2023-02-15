package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname WorkflowBusinessDTO
 * @Description TODO
 * @Date 2023-01-30 15:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WorkflowBusinessDTO implements Serializable {

    /**
     * 业务流程的key
     */
    @NotBlank(message = "业务key不能为空")
    private String businessKey;

    /**
     * 流程定义的key
     */
    @NotBlank(message = "业务流程定义的key不能为空")
    private String processDefinitionKey;

    /**
     * 在camunda 下画的流程图 在workflow resources 下的目录
     */
    @NotBlank(message = "bpmn名不能为空")
    private String bpmnName;

    /**
     * 业务名称
     */
    @NotBlank(message = "业务名称不能为空")
    private String businessName;

    /**
     * 业务属性
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;


    /**
     * 所需要的参数
     */
    @NotBlank(message = "业务所需要的参数不能为空")
    private String param;

    /**
     * 平台
     */
    @NotBlank(message = "业务平台不能为空")
    private String platform;
}
