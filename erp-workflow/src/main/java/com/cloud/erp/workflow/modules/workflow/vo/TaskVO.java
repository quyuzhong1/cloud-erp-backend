package com.cloud.erp.workflow.modules.workflow.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskVO
 * @Description TODO
 * @Date 2022-08-12 11:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskVO implements Serializable {
    private String taskId;

    private String assignee;

    //业务流程Id
    private String processInstanceId;

    //节点id
    private String nodeId;

    //意见
    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<String> comments;

}
