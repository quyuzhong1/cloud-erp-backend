package com.erp.model.plm.dto;

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
public class PreTaskDTO {
    /**
     * 前置任务id
     */
    private String preTaskId;
    /**
     * 依赖关系
     */
    private TaskRelationshipEnum relationshipCode;
    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;
}