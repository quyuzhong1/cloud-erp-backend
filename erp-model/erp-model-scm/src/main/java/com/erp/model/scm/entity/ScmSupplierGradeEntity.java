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
@TableName("scm_supplier_grade")
public class ScmSupplierGradeEntity extends BaseEntity<ScmSupplierGradeEntity> {

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
     * 当前等级
     */
    @TableField("current_grade")
    private String currentGrade;

    /**
     * 目标等级
     */
    @TableField("target_grade")
    private String targetGrade;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
