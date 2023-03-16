package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 供应商资质表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_supplier_credential")
public class ScmSupplierCredentialEntity extends BaseEntity<ScmSupplierCredentialEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;



    /**
     * 有效开始时间
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @TableField("expire_date")
    private LocalDate expireDate;

    /**
     * 供应商表id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
