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
 * 海外仓物流商 仓库表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_warehouse")
public class LogisticsWarehouseEntity extends BaseEntity<LogisticsWarehouseEntity> {

    /**
    * 物流商id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 海外仓库id 对应 wms overseas_provider_warehouse 表id
    */
    @TableField("overseas_warehouse_id")
    private String overseasWarehouseId;
    /**
    *  海外仓库名
    */
    @TableField("overseas_warehouse_name")
    private String overseasWarehouseName;
    /**
     *  海外 仓库code
     */
    @TableField("overseas_warehouse_code")
    private String overseasWarehouseCode;

    public static final String MAIN_ID = "main_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}