package com.erp.model.plm.vo;

import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreTaskVO {

    private String id;

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

    public PreTaskVO(TemplatePreTaskEntity templatePreTaskEntity) {
        this.id = templatePreTaskEntity.getId();
        this.taskId = templatePreTaskEntity.getTaskId();
        this.preTaskId = templatePreTaskEntity.getPreTaskId();
        this.relationshipCode = templatePreTaskEntity.getRelationship().getCode();
        this.relationshipName = templatePreTaskEntity.getRelationship().getName();
        this.intervalWorkPeriod = templatePreTaskEntity.getIntervalWorkPeriod();
    }

    public PreTaskVO(PreTaskEntity preTaskEntity) {
        this.id = preTaskEntity.getId();
        this.taskId = preTaskEntity.getTaskId();
        this.preTaskId = preTaskEntity.getPreTaskId();
        this.relationshipCode = preTaskEntity.getRelationship().getCode();
        this.relationshipName = preTaskEntity.getRelationship().getName();
        this.intervalWorkPeriod = preTaskEntity.getIntervalWorkPeriod();
    }
}