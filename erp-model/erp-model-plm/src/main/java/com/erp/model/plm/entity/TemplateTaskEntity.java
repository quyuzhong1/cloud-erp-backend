package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname TemplateTaskEntity

 * @Date 2022-09-20 15:29
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("template_task")
public class TemplateTaskEntity extends BaseEntity<TemplateTaskEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名
     */
    @TableField("name")
    private String name;

    @TableField("pid")
    private String pid;

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
     * 计划开始时间
     */
    @TableField("plan_start_time")
    private LocalDate planStartTime;

    /**
     * j计划结束时间
     */
    @TableField("plan_end_time")
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
     * 是否是固定任务 1 是  0  不是
     */
    @TableField("is_fixed")
    private Integer isFixed;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 引用任务的id
     */
    @TableField("source_task_id")
    private String sourceTaskId;

    /**
     * 模板表id
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 任务属性 1： 立项任务  2：项目任务
     */
    @TableField("property")
    private Integer property;

    //流程表id
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

    /**
     * 工期
     */
    @TableField("work_period")
    private Integer workPeriod;

}
