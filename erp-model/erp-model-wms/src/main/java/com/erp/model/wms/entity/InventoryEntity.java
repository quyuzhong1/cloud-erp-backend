package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Classname: InventoryEntity
 * @Description: 实时库存
 * @CreateTime: 2023-04-25  15:29
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("inventory")
public class InventoryEntity extends BaseEntity<InventoryEntity> implements Serializable {

    /**
     * 组织
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 仓库
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 状态编码
     */
    @TableField("dict_inventory_status")
    private String dictInventoryStatus;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

}