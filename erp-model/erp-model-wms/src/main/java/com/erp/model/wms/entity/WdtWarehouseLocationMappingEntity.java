package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 旺店通仓位映射
 * @date 2024-08-14
 * @author tanmujin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("wdt_warehouse_location_mapping")
public class WdtWarehouseLocationMappingEntity extends BaseEntity<WdtWarehouseLocationMappingEntity> {

    /**
     * ERP仓库ID
     */
    @TableField("sys_warehouse_id")
    private String sysWarehouseId;

    /**
     * ERP仓库编码
     */
    @TableField("sys_warehouse_code")
    private String sysWarehouseCode;

    /**
     * ERP仓位编码
     */
    @TableField("sys_warehouse_location")
    private String sysWarehouseLocation;

    /**
     * 第三方仓位编码
     */
    @TableField("third_warehouse_location")
    private String thirdWarehouseLocation;
}
