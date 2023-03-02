package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
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
public class ProjectPlanEntity  {
    private static final long serialVersionUID = 240838215899434939L;


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    /**
     * 逻辑删除字段
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;

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
     * 状态  waitSubmit 待提交 waitAudit 待审核 auditIng 审核中 auditNoPass 审核不通过， auditPass 审核通过
     */
    private String status;
    /**
     * 任务数量
     */
    private Integer taskQuantity;
    /**
     * 备注
     */
    private String remark;


}

