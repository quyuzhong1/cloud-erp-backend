package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 虚拟仓实体仓关联关系
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_relation")
public class VirtualWarehouseRelationEntity extends BaseEntity<VirtualWarehouseRelationEntity> {

    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 实体仓id
    */
    @TableField("warehouse_id")
    private String warehouseId;


    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}