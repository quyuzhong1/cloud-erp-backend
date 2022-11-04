package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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
public class ProjectTaskSysEntity implements Serializable {

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
    private Date planStartTime;

    /**
     * j计划结束时间
     */
    @TableField(value = "plan_end_time" ,insertStrategy =FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
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



    @TableField("property")
    private Integer property;

    /**
     * 是否是固定任务 1 是  2  不是
     */
    @TableField("is_fixed")
    private Integer isFixed;

    /**
     * 审核人id 多个以逗号分割
     */
    @TableField("approval_user_id")
    private String approvalUserId;
    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    @TableField("create_user_id")
    private String createUserId;

    @TableField("create_user_name")
    private String createUserName;

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

    @TableField("business_process_id")
    private String businessProcessId;


}
