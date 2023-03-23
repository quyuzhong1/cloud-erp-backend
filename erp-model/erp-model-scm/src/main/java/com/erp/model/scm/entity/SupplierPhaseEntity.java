package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
     * 操作类型
     */
    @TableField("type")
    private String type;

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
     * 审核状态 
     */
    @TableField(value="approve_status" )
    private String approveStatus;


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
