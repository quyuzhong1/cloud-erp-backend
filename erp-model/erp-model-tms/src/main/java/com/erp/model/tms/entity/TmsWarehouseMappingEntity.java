package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_warehouse_mapping")
public class TmsWarehouseMappingEntity extends BaseEntity<TmsWarehouseMappingEntity> {

    /**
    * 仓库代码（物流商）
    */
    @TableField("logistics_warehouse_code")
    private String logisticsWarehouseCode;
    /**
    * 仓库id（数大臣）
    */
    @TableField("erp_warehouse_id")
    private String erpWarehouseId;
    /**
    * 仓库名称（数大臣）
    */
    @TableField("erp_warehouse_name")
    private String erpWarehouseName;


    public static final String LOGISTICS_WAREHOUSE_CODE = "logistics_warehouse_code";

    public static final String ERP_WAREHOUSE_ID = "erp_warehouse_id";

    public static final String ERP_WAREHOUSE_NAME = "erp_warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}