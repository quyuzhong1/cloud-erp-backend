package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目计划表(ProjectPlan)实体类
 *
 * @author yl
 * @since 2023-02-03 14:45:42
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_plan")
public class ProjectPlanEntity implements Serializable {
    private static final long serialVersionUID = 240838215899434939L;
    
    private String id;
    /**
     * 创建人id
     */
    private String createUserId;
    /**
     * 创建人name
     */
    private String createUserName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更改人id
     */
    private String updateUserId;
    /**
     * 更改人名
     */
    private String updateUserName;
    /**
     * 时间
     */
    private Date updateTime;
    /**
     * 产品id
     */
    private String productId;
    /**
     * 类型 change 变更  normal  正常
     */
    private String type;
    /**
     * 阶段 projectApproval 立项阶段,project项目阶段
     */
    private String phase;
    /**
     * 审核完成时间
     */
    private Date approvalFinishTime;
    /**
     * 状态 0 待审核
     */
    private Integer state;
    /**
     * 任务数量
     */
    private Integer taskQuantity;



}

