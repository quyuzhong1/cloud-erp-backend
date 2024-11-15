package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;

import javax.validation.constraints.NotBlank;


/**
 * <p>
 * 仓位移动明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("warehouse_location_move_detail")
public class WarehouseLocationMoveDetailEntity extends BaseEntity<WarehouseLocationMoveDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku表id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 取货仓位
    */
    @TableField("out_warehouse_location")
    private String outWarehouseLocation;
    /**
     * 取货仓位
     */
    @TableField("out_inventory_status")
    private String outInventoryStatus;
    /**
    * 上架仓位
    */
    @TableField("in_warehouse_location")
    private String inWarehouseLocation;
    /**
     * 上架仓位库存状态
     */
    @TableField("in_inventory_status")
    private String inInventoryStatus;
    /**
    * 移动数量
    */
    @TableField("qty")
    private Integer qty;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
     * 库存组织id
     */
    @TableField("inventory_org_id")
    private String inventoryOrgId;
    /**
     * 库存组织名称
     */
    @TableField("inventory_org_name")
    private String inventoryOrgName;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String OUT_WAREHOUSE_LOCATION = "out_warehouse_location";

    public static final String IN_WAREHOUSE_LOCATION = "in_warehouse_location";

    

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    
    @Override
    public Serializable pkVal() {
        return null;
    }

}