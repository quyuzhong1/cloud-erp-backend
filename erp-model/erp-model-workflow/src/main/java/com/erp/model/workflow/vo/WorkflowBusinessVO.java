package com.erp.model.workflow.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname WorkflowBusinessVO
 * @Description TODO
 * @Date 2023-01-30 16:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WorkflowBusinessVO implements Serializable {


    /**
     * 表id
     */
    private String id;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 业务key
     */
    private String businessKey;


    /**
     * 业务名称
     */
    private String param;


    /**
     * 审核人数
     */
    private Integer auditorTotal;

}
