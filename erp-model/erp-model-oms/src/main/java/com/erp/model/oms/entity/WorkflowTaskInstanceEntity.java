package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 任务编排实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("workflow_task_instance")
public class WorkflowTaskInstanceEntity extends BaseEntity<WorkflowTaskInstanceEntity> {

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private String sourceId;

    @TableField("source_code")
    private String sourceCode;

    /**
     * WorkflowTaskInstanceStatusEnum
     */
    @TableField("status")
    private String status;

    @TableField("current_index")
    private Integer currentIndex;

    @TableField("total_steps")
    private Integer totalSteps;

    @TableField("trace_id")
    private String traceId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("last_error")
    private String lastError;
}
