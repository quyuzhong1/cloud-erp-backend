package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 供应商升降级
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("supplier_phase")
public class SupplierPhaseEntity extends BaseEntity<SupplierPhaseEntity> {

    /**
     * 供应商表id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 当前阶段
     */
    @TableField(value="current_phase")
    private String currentPhase;

    /**
     * 目标阶段
     */
    @TableField(value="target_phase")
    private String targetPhase;

    /**
     * 当前等级
     */
    @TableField(value="current_grade_id")
    private String currentGradeId;

    /**
     * 目标等级
     */
    @TableField(value="target_grade_id")
    private String targetGradeId;

    /**
     * 审核状态 
     */
    @TableField(value="approve_status" )
    private String approveStatus;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 说明
     */
    @TableField("description")
    private String description;



    @Override
    public Serializable pkVal() {
        return null;
    }

}
