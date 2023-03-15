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
@TableName("scm_supplier_aptitudes")
public class ScmSupplierAptitudesEntity extends BaseEntity<ScmSupplierAptitudesEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;



    /**
     * 有效开始时间
     */
    @TableField("valid_start_time")
    private LocalDate validStartTime;

    /**
     * 有效结束时间
     */
    @TableField("valid_end_time")
    private LocalDate validEndTime;

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


    public static final String NAME = "name";

    public static final String VALID_START_TIME = "valid_start_time";

    public static final String VALID_END_TIME = "valid_end_time";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
