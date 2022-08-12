package com.cloud.erp.workflow.modules.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * @Classname 启动流程参数
 * @Description TODO
 * @Date 2022-08-11 11:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StartProcessDTO implements Serializable {

    @NotBlank(message = "流程定义的key 不能为空")
    //对应流程图bpmn process id="Process_0f68eu1" 后面会是在表或者常量中
    private String processDefinitionKey;

    //业务的 key  自定义
    private String businessKey;

    @NotBlank(message = "发起人不能为空")
    //业务发起人
    private String initiator;


    private Map<String,Object> map;
}
