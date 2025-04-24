package com.erp.model.plm.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.PlanTaskNameDTO;
import com.erp.model.plm.dto.ProjectChildTaskDTO;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目计划任务表(ProjectPlanTask)实体类
 *
 * @author yl
 * @since 2023-02-03 14:45:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_plan_task")
@NoArgsConstructor
public class ProjectPlanTaskEntity extends BaseEntity<ProjectPlanTaskEntity> {
    private static final long serialVersionUID = 184565397899617521L;

    /**
     * 产品id
     */
    private String productId;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 源计划开始时间
     */
    private LocalDate originStartTime;
    /**
     * 源计划结束时间
     */
    private LocalDate originEndTime;
    /**
     * 变更后计划开始时间
     */
    private LocalDate changeStartTime;
    /**
     * 变更后计划结束时间
     */
    private LocalDate changeEndTime;
    /**
     * 项目变更表id
     */
    private String projectPlanId;

    /**
     * 源负责人id
     */
    private String originChargeId;

    /**
     * 变更负责人
     */
    private String changeChargeId;

    /**
     * 是否重启
     */
    private Boolean isRestart;
    /**
     * 工期
     */
    @TableField("work_period")
    private Integer workPeriod;

    public ProjectPlanTaskEntity(PlanTaskNameDTO planTaskNameDTO, LocalDate startDate, List<LocalDate> dateList) {
        super(planTaskNameDTO.getPlanId());
        Integer planWorkPeriod = planTaskNameDTO.getWorkPeriod();
        LocalDate endDate = startDate;
        while (planWorkPeriod > 1){
            endDate = endDate.plusDays(1);
            if(!dateList.contains(endDate)){
                planWorkPeriod --;
            }
        }
        this.changeStartTime = startDate;
        this.changeEndTime = endDate;
    }

    public ProjectPlanTaskEntity(String taskId, LocalDate startDate, Integer workPeriod, List<LocalDate> dateList, Integer type) {
        super(taskId);
        LocalDate endDate = startDate;
        while (workPeriod > 0){
            if(!dateList.contains(startDate.plusDays(1))){
                endDate = startDate.plusDays(1);
                workPeriod --;
            }
        }
        if(1 == type){
            this.originStartTime = startDate;
            this.originStartTime = endDate;
        }else {
            this.changeStartTime = startDate;
            this.changeEndTime = endDate;
        }
        this.workPeriod = workPeriod;
    }

    public ProjectPlanTaskEntity(PlanTaskNameDTO task, LocalDate startDate, LocalDate endDate, ProjectChildTaskDTO projectChildTaskDTO, List<LocalDate> dateList) {
        super(task.getPlanId());
        TaskRelationshipEnum relationship = projectChildTaskDTO.getRelationship();
        Integer intervalWorkPeriod = projectChildTaskDTO.getIntervalWorkPeriod();
        Integer planWorkPeriod = task.getWorkPeriod();
        Map<String, LocalDate> resultMap = LocalDateUtil.relationshipLocalDate(relationship.getCode(), startDate, endDate, intervalWorkPeriod, planWorkPeriod, dateList);
        this.changeStartTime = resultMap.get("startDate");
        this.changeEndTime = resultMap.get("endDate");
    }
}

