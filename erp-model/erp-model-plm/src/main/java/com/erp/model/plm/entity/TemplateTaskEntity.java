package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname TemplateTaskEntity
 * @Description TODO
 * @Date 2022-09-20 15:29
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("template_task")
public class TemplateTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
     * 前置任务id
     */
    @TableField("pre_task_id")
    private String preTaskId;

    /**
     * 计划开始时间
     */
    @TableField("plan_start_time")
    private Date planStartTime;

    /**
     * j计划结束时间
     */
    @TableField("plan_end_time")
    private Date planEndTime;

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
     * 是否是系统任务 1 是  0  不是
     */
    @TableField("is_fixed")
    private Integer isFixed;




    /**
     * 任务描述
     */
    @TableField("description")
    private String description;



    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

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
     * 模板表id
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 任务属性 1： 立项任务  2：项目任务
     */
    @TableField("property")
    private Integer property;




}
