package com.erp.model.plm.dto;

import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

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
    private String relationshipCode;
    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;

    @Data
    @NoArgsConstructor
    public static class ListPreTaskDTO{
        @NotBlank(message = "任务id不能为空")
        private String taskId;
    }
}