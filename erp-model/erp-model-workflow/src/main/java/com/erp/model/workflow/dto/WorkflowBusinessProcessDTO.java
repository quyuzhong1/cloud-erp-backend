package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Classname SaveWorkflowBusinessProcessDTO
 * @Description TODO
 * @Date 2023-01-31 17:01
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WorkflowBusinessProcessDTO implements Serializable
{


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUserId;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 具体业务表id
     */
    private String businessTableId;

    /**
     *
     */
    private String businessId;




}
