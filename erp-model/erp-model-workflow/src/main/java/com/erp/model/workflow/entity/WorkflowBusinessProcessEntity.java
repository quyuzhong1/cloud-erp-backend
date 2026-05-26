package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Classname WorkflowBusinessProcessEntity
 * @Date 2023-01-30 15:09
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_business_process")
public class WorkflowBusinessProcessEntity extends BaseEntity<WorkflowBusinessProcessEntity> {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 业务表id
     * 该 id 是具体的业务表的id
     */
    private String businessTableId;

    /**
     * 业务表id
     * workflow_business
     */
    private String businessId;
}
