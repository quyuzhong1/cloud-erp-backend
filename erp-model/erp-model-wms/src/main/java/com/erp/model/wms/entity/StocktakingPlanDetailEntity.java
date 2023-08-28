package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 盘点计划明细表
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("stocktaking_plan_detail")
public class StocktakingPlanDetailEntity extends BaseEntity<StocktakingPlanDetailEntity> {


    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;

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
    * 仓库区域
    */
    @TableField("warehouse_area")
    private String warehouseArea;

    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
    * skuid
    */
    @TableField("sku_id")
    private String skuId;

    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * 组织id
    */
    @TableField("org_id")
    private String orgId;

    /**
    * 组织名称
    */
    @TableField("org_name")
    private String orgName;

    /**
     * 仓位id
     */
    @TableField("warehouse_location_id")
    private String warehouseLocationId;

    /**
     * 库存id
     */
    @TableField("inventory_id")
    private String inventoryId;


    public static final String MAIN_ID = "main_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_AREA = "warehouse_area";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    public static final String WAREHOUSE_LOCATION_ID = "warehouse_location_id";

    public static final String INVENTORY_ID = "inventory_id";

    public StocktakingPlanDetailEntity(StocktakingPlanDTO.DetailDTO item, String mainId, WarehouseDTO.UpdateDTO warehouse, String orgName) {
        super(item.getId());
        this.mainId = mainId;
        this.warehouseId = item.getWarehouseId();
        this.warehouseName = warehouse.getName();
        this.warehouseArea = item.getWarehouseArea();
        this.warehouseLocation = item.getWarehouseLocation();
        this.skuId = item.getSkuId();
        this.skuNo = item.getSkuNo();
        this.orgId = warehouse.getOrgId();
        this.orgName = orgName;
        this.warehouseLocationId = item.getWarehouseLocationId();
        this.inventoryId = item.getInventoryId();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}