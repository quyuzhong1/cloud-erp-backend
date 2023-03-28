package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import com.erp.model.plm.dto.PreTaskUpdateDTO;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 任务的前置任务表
 * @TableName template_pre_task
 */
@Data
@TableName(value ="template_pre_task")
@NoArgsConstructor
public class TemplatePreTaskEntity extends BaseEntity implements Serializable {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 前置任务id
     */
    private String preTaskId;

    /**
     * 模板id
     */
    private String templateId;

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


    public TemplatePreTaskEntity(PreTaskUpdateDTO entity){
        super(entity.getId());
        this.relationship = TaskRelationshipEnum.getByCode(entity.getRelationshipCode());
        this.intervalWorkPeriod = entity.getIntervalWorkPeriod();
    }

    public TemplatePreTaskEntity(String preTask, String taskId, String templateId, TemplatePreTaskEntity templatePreTaskEntity) {
        this.taskId = taskId;
        this.templateId = templateId;
        this.preTaskId = preTask;
        if(null != templatePreTaskEntity){
            this.intervalWorkPeriod = templatePreTaskEntity.getIntervalWorkPeriod();
            this.relationship = templatePreTaskEntity.getRelationship();
        }
    }
}