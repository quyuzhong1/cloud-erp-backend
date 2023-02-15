package com.erp.model.workflow.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname MyToDoTaskVO
 * @Description TODO
 * @Date 2023-01-31 10:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MyToDoTaskVO implements Serializable {

    private String taskId;

    private String assignee;

    //业务流程Id
    private String processInstanceId;

    //节点id
    private String nodeId;

    /**
     * 业务表id
     */
    private String businessTableId;


    //意见
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<String> comments;
}
