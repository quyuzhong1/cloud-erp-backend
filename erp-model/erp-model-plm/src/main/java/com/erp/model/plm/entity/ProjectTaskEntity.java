package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @TableField("person_in_charge_id")
    private String personInChargeId;

    /**
     * 负责人名
     */
    @TableField("person_in_charge")
    private String personInCharge;

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
     * 是否是系统任务 1 是  2  不是
     */
    @TableField("is_sys")
    private Integer isSys;


    /**
     * 任务状态 0:待发布 1:未开始 2:进行中 3 已完成,4.已关闭
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

    //审核任务状态 0:待发布,1:待审核(未开始)，2:进行中 3 审核通过 4.审核不通过  5 关闭
    @TableField("task_approval_status")
    private Integer taskApprovalStatus;

    @TableField("task_finish_time")
    private Date  taskFinishTime;


}
