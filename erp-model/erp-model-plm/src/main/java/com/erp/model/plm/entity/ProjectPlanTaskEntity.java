package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目计划任务表(ProjectPlanTask)实体类
 *
 * @author yl
 * @since 2023-02-03 14:45:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_plan_task")
public class ProjectPlanTaskEntity implements Serializable {
    private static final long serialVersionUID = 184565397899617521L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建人id
     */
    private String createUserId;
    /**
     * 创建名
     */
    private String createUserName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更改人
     */
    private String updateUserId;
    /**
     * 更改人名
     */
    private String updateUserName;
    /**
     * 更改时间
     */
    private Date updateTime;
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
    private Date originStartTime;
    /**
     * 源计划结束时间
     */
    private Date originEndTime;
    /**
     * 变更后计划开始时间
     */
    private Date changeStartTime;
    /**
     * 变更后计划结束时间
     */
    private Date changeEndTime;
    /**
     * 项目变更表id
     */
    private String projectPlanId;



}

