package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
@TableName("supplier_credential")
public class SupplierCredentialEntity extends BaseEntity<SupplierCredentialEntity> {

    /**
     * 资质编码
     */
    @TableField("code")
    private String code;
    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 有效开始时间
     */
    @TableField(value = "effective_date", updateStrategy = FieldStrategy.IGNORED)
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @TableField(value = "expire_date", updateStrategy = FieldStrategy.IGNORED)
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


    /**
     * SupplierCredentialStatusEnum 生效状态：notEffective=未生效,effective=生效中,expired=失效
     */
    @TableField("status")
    private String status;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
