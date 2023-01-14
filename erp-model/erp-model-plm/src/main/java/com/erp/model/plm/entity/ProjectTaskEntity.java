package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

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
public class ProjectTaskEntity implements Serializable {

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
    @TableField(value = "plan_start_time",insertStrategy =FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
    private Date planStartTime;

    /**
     * j计划结束时间
     */
    @TableField(value = "plan_end_time",insertStrategy =FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
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
     * 审核人id 多个以逗号分割
     */
    @TableField("approval_user_id")
    private String approvalUserId;


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
    private Date realityStartTime;

    @TableField("reality_end_time")
    private Date realityEndTime;

    @TableField("pid")
    private String pid;

    @TableField("create_user_id")
    private String createUserId;

    @TableField("create_user_name")
    private String createUserName;

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
     * 辅助字段：是否完成
     */
    @TableField(exist=false)
    private Integer isfinish;

    /**
     * 审核角色id 多个以逗号分割，由模板生成时带过来
     */
    @TableField("approval_role_name")
    private String approvalRoleName;

    /**
     * 审核角色id 多个以逗号分割，由模板生成时带过来
     */
    @TableField("superior_type")
    private String superiorType;

    /**
     * 审核分配类型，由模板生成时带过来（0角色，1人员，2上级人员负责人）
     */
    @TableField("approval_distribution_type")
    private Integer approvalDistributionType;
}
