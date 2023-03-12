package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.erp.model.plm.dto.PreTaskDTO;
import com.erp.model.plm.dto.PreTaskUpdateDTO;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务的前置任务表
 * @TableName pre_task
 */
@TableName(value ="pre_task")
@Data
@NoArgsConstructor
public class PreTaskEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 前置任务id
     */
    private String preTaskId;


    /**
     * 产品id
     */
    private String productId;

    /**
     * 
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 间隔工期
     */
    @TableField(value = "interval_work_period")
    private Integer intervalWorkPeriod;

    /**
     * 依赖关系
     */
    @TableField(value = "relationship")
    private TaskRelationshipEnum relationship;

    private static final long serialVersionUID = 1L;

    public PreTaskEntity(String preTaskId, String taskId, String productId,PreTaskEntity entity) {
        this.preTaskId = preTaskId;
        this.taskId = taskId;
        this.productId = productId;
        if(null != entity){
            this.intervalWorkPeriod = entity.getIntervalWorkPeriod();
            this.relationship = entity.getRelationship();
        }
    }

    public PreTaskEntity(PreTaskUpdateDTO updateDTO) {
        this.id = updateDTO.getId();
        this.intervalWorkPeriod = updateDTO.getIntervalWorkPeriod();
        this.relationship = TaskRelationshipEnum.getByCode(updateDTO.getRelationshipCode());
    }
}