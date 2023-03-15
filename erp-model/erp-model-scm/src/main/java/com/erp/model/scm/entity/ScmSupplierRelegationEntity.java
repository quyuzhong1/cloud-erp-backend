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
@TableName("scm_supplier_relegation")
public class ScmSupplierRelegationEntity extends BaseEntity<ScmSupplierRelegationEntity> {

    /**
     * 供应商表id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 当前阶段
     */
    @TableField("current_phase")
    private String currentPhase;

    /**
     * 目标
     */
    @TableField("target_phase")
    private String targetPhase;

    /**
     * 审核状态 
     */
    @TableField("audit_status")
    private String auditStatus;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String OPERATION_TYPE = "operation_type";

    public static final String CURRENT_PHASE = "current_phase";

    public static final String TARGET_PHASE = "target_phase";

    public static final String AUDIT_STATUS = "audit_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
