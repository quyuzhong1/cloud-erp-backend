package com.erp.model.wms.entity;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 盘点任务明细表
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@TableName("stocktaking_task_detail")
public class StocktakingTaskDetailEntity extends BaseEntity<StocktakingTaskDetailEntity> {

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
     * 库位编码
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 可用库存
     */
    @TableField("usable_qty")
    private Integer usableQty;

    /**
     * 盘点数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 冻结数量
     */
    @TableField("frozen_qty")
    private Integer frozenQty;

    /**
     * 差异数量
     */
    @TableField("diff_qty")
    private Integer diffQty;


    public static final String MAIN_ID = "main_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String USABLE_QTY = "usable_qty";

    

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String DIFF_QTY = "diff_qty";

    public StocktakingTaskDetailEntity(List<InventoryEntity> inventoryEntities, String mainId, String warehouseName) {
        this.mainId = mainId;
        this.warehouseId = inventoryEntities.get(0).getWarehouseId();
        this.warehouseName = warehouseName;
        this.warehouseLocation = inventoryEntities.get(0).getWarehouseLocation();
        this.skuId = inventoryEntities.get(0).getSkuId();
        this.skuNo = inventoryEntities.get(0).getSkuNo();
        inventoryEntities.stream().forEach(item -> {
            if (ObjectUtil.equals(InventoryStatusEnum.USABLE.getCode(), item.getDictInventoryStatus())) {
                this.usableQty = item.getQty();
            }
            if (ObjectUtil.equals(InventoryStatusEnum.FROZEN.getCode(), item.getDictInventoryStatus())) {
                this.frozenQty = item.getQty();
            }
        });
        this.diffQty = 0- (Objects.isNull(usableQty)?0:usableQty) - (Objects.isNull(frozenQty)?0:frozenQty);
    }

    public StocktakingTaskDetailEntity(List<InventoryEntity> inventoryEntities, String id, String warehouseName, String uid, String username) {
        this(inventoryEntities, id, warehouseName);
        super.setCreateUserId(uid);
        super.setCreateUserName(username);
        super.setUpdateUserId(uid);
        super.setUpdateUserName(username);
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
