package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 部署流程入参
 *
 * @Description TODO
 * @Date 2022-08-11 11:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeployProcessDTO implements Serializable {


    //流程bpmn 名
    @NotBlank(message = "流程名不能为空")
    private String bpmnName;

    @NotBlank(message = "目录下的流程图不能为空")
    private String businessName;


}
