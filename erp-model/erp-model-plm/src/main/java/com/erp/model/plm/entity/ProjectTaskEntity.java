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
 * <p>
 * 产品任务表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_task")
@NoArgsConstructor
public class ProjectTaskEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名
     */
    @TableField("name")
    private String name;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    @TableField("type")
    private Integer type;

    /**
     * 负责人id
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     * 负责人名
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 角色名称，由模板生成时带过来
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 分配类型，由模板生成时带过来（0角色，1人员）
     */
    @TableField("distribution_type")
    private Integer distributionType;

    /**
     * 计划开始时间
     */
    @TableField(value = "plan_start_time", insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private LocalDate planStartTime;

    /**
     * j计划结束时间
     */
    @TableField(value = "plan_end_time", insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private LocalDate planEndTime;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 任务阶段id
     */
    @TableField("phase_id")
    private String phaseId;

    @TableField("phase_name")
    private String phaseName;


    /**
     * 是否是固定任务 1是  0  不是
     */
    @TableField("is_fixed")
    private Integer isFixed;


    /**
     * 任务状态 任务状态 0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
     */
    @TableField("status")
    private Integer status;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 项目Id
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 流程id
     */
    @TableField("process_id")
    private String processId;

    /**
     * 引用系统任务的id
     */
    @TableField("quote_sys_task_id")
    private String quoteSysTaskId;

    /**
     * 产品表id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 任务属性 1： 立项任务  2：项目任务
     */
    @TableField("property")
    private Integer property;


    @TableField("reality_start_time")
    private LocalDateTime realityStartTime;

    @TableField("reality_end_time")
    private LocalDateTime realityEndTime;

    @TableField("pid")
    private String pid;

    //流程表id
    @TableField("business_process_id")
    private String businessProcessId;

    /**
     * 设置里程碑(0否，1是)
     */
    @TableField("is_milepost")
    private Integer isMilepost;

    /**
     * 否有对sku 进行变更 默认没有 0 没有 1  有
     */
    @TableField("is_sku_change")
    private Integer isSkuChange;

    /**
     * 关联sku类型,RelatedSkuTypeEnum枚举(1，自动关联，2选择关联，3不关联)
     */
    @TableField("related_sku_type")
    private String relatedSkuType;

    /**
     * 辅助字段：是否完成
     */
    @TableField(exist = false)
    private Integer isfinish;

    /**
     * 辅助字段：前任务id
     */
    @TableField(exist = false)
    private String prevId;

    /**
     * 计划状态
     */
    @TableField("schedule_status")
    private String scheduleStatus;

    /**
     * 排期类型
     */
    @TableField("schedule_type")
    private String scheduleType;

    /**
     * 工期
     */
    @TableField("work_period")
    private Integer workPeriod;

    public ProjectTaskEntity(String taskId, LocalDateTime realityStart, LocalDateTime realityEnd) {
        super(taskId);
        if(null != realityEnd){
            this.realityEndTime = realityEnd;
        }
        if (null != realityStart) {
            this.realityStartTime = realityStart;
        }
    }

    public ProjectTaskEntity(String id, Integer workPeriod) {
        super(id);
        this.workPeriod = workPeriod;
    }
    public ProjectTaskEntity(PlanTaskNameDTO task, LocalDate startDate, List<LocalDate> dateList) {
        super(task.getId());
        Integer planWorkPeriod = task.getWorkPeriod();
        LocalDate endDate = startDate;
        while (planWorkPeriod > 1){
            endDate =  endDate.plusDays(1);
            if(!dateList.contains(endDate)){
                planWorkPeriod --;
            }
        }
        this.planStartTime =  startDate;
        this.planEndTime =  endDate;
    }
    public ProjectTaskEntity(PlanTaskNameDTO task, LocalDate startDate, LocalDate endDate, ProjectChildTaskDTO projectChildTaskDTO, List<LocalDate> dateList) {
        super(task.getId());
        TaskRelationshipEnum relationship = projectChildTaskDTO.getRelationship();
        Integer intervalWorkPeriod = projectChildTaskDTO.getIntervalWorkPeriod();
        Integer planWorkPeriod = task.getWorkPeriod();
        Map<String, LocalDate> resultMap = LocalDateUtil.relationshipLocalDate(relationship.getCode(), startDate, endDate, intervalWorkPeriod, planWorkPeriod, dateList);
        this.planStartTime = resultMap.get("startDate");
        this.planEndTime = resultMap.get("endDate");
    }
}
