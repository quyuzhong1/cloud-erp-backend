package com.common.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * @description:project封装类
 * @author Will
 * @date: 2023/6/1 16:13
 */
@Data
@NoArgsConstructor
public class ProjectImportDTO {

    /**
     * 前置任务ID
     */
    private String preTask;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务项名称
     */
    private String taskName;

    /**
     * 任务级别
     */
    private Integer taskOutlineLevel;

    /**
     * 父任务ID
     */
    private String taskParentDefId;

    /**
     * 阶段完成百分比
     */
    private String taskPercentage;

    /**
     * 计划开始日期
     */
    private LocalDate taskStartDate;

    /**
     * 任务唯一ID
     */
    private String taskUniqueId;

    /**
     * WBS码
     */
    private String taskWbs;

    /**
     * 阶段计划工作量
     */
    private String taskWork;

    /**
     * 阶段状态
     */
    private String taskWorkStatus;

    /**
     * 计划工作量单位
     */
    private String taskWorkUnits;

    /**
     * 实际完成日期
     */
    private LocalDate taskActualFinishDate;

    /**
     * 实际开始日期
     */
    private LocalDate taskActualStartDate;

    /**
     * 实际阶段工作量
     */
    private String taskActualWork;

    /**
     * 实际工作量单位
     */
    private String taskActualWorkUnits;

    /**
     * 阶段计划工期
     */
    private String taskDuration;

    /**
     * 计划工期单位
     */
    private String taskDurationUnits;

    /**
     * 计划完成日期
     */
    private LocalDate taskFinishDate;

    /**
     * 自定义字段
     * Map<接收字段名称,返回值>
     */
    private Map<String,String> customFieldValues;


}
