package com.erp.model.plm.dto;

import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 关联前置任务
 * @Description TODO
 * @Date 2022-09-22 18:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SetPreTaskDTO implements Serializable {

    /**
     * 任务id
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    /**
     * 前置任务id
     */
    @NotBlank(message = "前置任务id 不能为空")
    private String preTaskId;

    /**
     * 依赖关系
     */
    private String relationshipCode;
    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;
}
