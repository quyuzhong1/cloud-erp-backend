package com.erp.model.plm.vo;

import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreTaskListVO {

    /**
     * id
     */
    private String id;
    /**
     * 前置任务名称
     */
    private String preTaskName;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 前置任务id
     */
    private String preTaskId;
    /**
     * 依赖关系编码
     */
    private String relationshipCode;
    /**
     * 依赖关系名称
     */
    private String relationshipName;
    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;

    public PreTaskListVO(PreTaskEntity entity) {
        this.id = entity.getId();
        this.taskId = entity.getTaskId();
        this.preTaskId = entity.getPreTaskId();
        this.relationshipCode = entity.getRelationship().getCode();
        this.relationshipName = entity.getRelationship().getName();
        this.intervalWorkPeriod = entity.getIntervalWorkPeriod();
    }

    public PreTaskListVO(PreTaskEntity entity, ProjectTaskEntity preTask) {
        this.id = entity.getId();
        this.taskId = entity.getTaskId();
        this.preTaskId = entity.getPreTaskId();
        this.relationshipCode = entity.getRelationship().getCode();
        this.relationshipName = entity.getRelationship().getName();
        this.intervalWorkPeriod = entity.getIntervalWorkPeriod();
        if(null != preTask){
            this.preTaskName = preTask.getName();
        }
    }
}