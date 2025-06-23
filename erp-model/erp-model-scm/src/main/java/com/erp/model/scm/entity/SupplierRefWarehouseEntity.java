package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 供应商关联仓库表
 * </p>
 *
 * @author will
 * @since 2025-06-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("supplier_ref_warehouse")
public class SupplierRefWarehouseEntity extends BaseEntity<SupplierRefWarehouseEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
     * 供应商名称
     */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 仓位编码
    */
    @TableField("warehouse_location_code")
    private String warehouseLocationCode;
    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 供应商名称
     */
    @TableField(exist = false)
    private String supplierName;

    /**
     * 仓库名称
     */
    @TableField(exist = false)
    private String warehouseName;

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String DISABLE = "disable";

    public static final String WAREHOUSE_LOCATION_CODE = "warehouse_location_code";

    public static final String INDEX = "index";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}