package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 系统任务
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_task_sys")
public class ProjectTaskSysEntity extends BaseEntity implements Serializable {

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
     * 负责人id 多个
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     * 负责人名
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 前置任务id
     */
    @TableField("pre_task_id")
    private String preTaskId;

    /**
     * 计划开始时间
     */
    @TableField(value = "plan_start_time" ,insertStrategy = FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime planStartTime;

    /**
     * j计划结束时间
     */
    @TableField(value = "plan_end_time" ,insertStrategy =FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime planEndTime;

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

    @TableField("property")
    private Integer property;

    /**
     * 是否是固定任务 1 是  2  不是
     */
    @TableField("is_fixed")
    private Integer isFixed;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    @TableField("business_process_id")
    private String businessProcessId;

    /**
     * 设置里程碑(0否，1是)
     */
    @TableField("is_milepost")
    private Integer isMilepost;

    /**
     * 角色id
     */
    @TableField("role_id")
    private String roleId;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 分配类型（0角色，1人员）
     */
    @TableField("distribution_type")
    private Integer distributionType;

    /**
     * 关联sku类型,RelatedSkuTypeEnum枚举(1，自动关联，2选择关联，3不关联)
     */
    @TableField("related_sku_type")
    private String relatedSkuType;


    @TableField("template_id")
    private String templateId;

    /**
     * 工期
     */
    @TableField("work_period")
    private Integer workPeriod;
}
