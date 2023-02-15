package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板分配
 * @date 2023/1/29 14:23
 */
@Data
@TableName(value ="task_charge_distribution")
public class TaskChargeDistributionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 任务id
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 分配类型（0角色，1人员，2上级人员负责人）,枚举（DistributionTypeEnum）
     */
    @TableField("distribution_type")
    private Integer distributionType;

    /**
     *  人员储存id，角色储存名称，上级人员负责人储存枚举值（ChargeSuperiorEnum）
     */
    @TableField("charges")
    private String charges;

    /**
     * 负责人id,逗号分隔
     */
    @TableField("charge_ids")
    private String chargeIds;

    /**
     * 数据来源（1系统任务，2模板任务，3任务列表）
     */
    @TableField("source")
    private Integer source;

    /**
     * 序号
     */
    @TableField("seq")
    private Integer seq;

}
