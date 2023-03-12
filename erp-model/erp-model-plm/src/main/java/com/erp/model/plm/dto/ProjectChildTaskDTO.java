package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 项目子任务DTO
 *
 * @Author Cloud
 * @Date 2023/2/28 10:06
 **/

@Data
@NoArgsConstructor
public class ProjectChildTaskDTO {

    private String id;

    /**
     * 任务名
     */
    private String name;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer type;

    /**
     * 任务状态 任务状态 0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
     */
    private Integer status;


    /**
     * 任务属性 1： 立项任务  2：项目任务
     */
    private Integer property;


    private LocalDateTime realityStartTime;

    private LocalDateTime realityEndTime;

    /**
     * 计划状态
     */
    private String scheduleStatus;

    /**
     * 排期类型
     */
    private String scheduleType;

    /**
     * 工期
     */
    private Integer workPeriod;

    /**
     * 前置任务关系id
     */
    private String preId;

    /**
     * 前置任务id
     */
    private String preTaskId;

    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;

    /**
     * 依赖关系
     */
    private TaskRelationshipEnum relationship;

    public ProjectChildTaskDTO(ProjectTaskEntity task, PreTaskEntity entity) {
        this.id = task.getId();
        this.name = task.getName();
        this.type = task.getType();
        this.status = task.getStatus();
        this.property = task.getPriority();
        this.realityStartTime = task.getRealityStartTime();
        this.realityEndTime = task.getRealityEndTime();
        this.scheduleStatus = task.getScheduleStatus();
        this.scheduleType = task.getScheduleType();
        this.workPeriod = task.getWorkPeriod();
        this.preId = entity.getId();
        this.preTaskId = entity.getPreTaskId();
        this.intervalWorkPeriod = entity.getIntervalWorkPeriod();
        this.relationship = entity.getRelationship();
    }
}
