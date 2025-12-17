package com.erp.model.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 库龄分析表
 * </p>
 *
 * @author will
 * @since 2025-08-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_inventory_age")
public class VirtualInventoryAgeEntity extends BaseEntity<VirtualInventoryAgeEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 虚拟仓库存
    */
    @TableField("virtual_qty")
    private Integer virtualQty;
    /**
    * 虚拟仓可用库存
    */
    @TableField("virtual_usable_qty")
    private Integer virtualUsableQty;
    /**
    * 虚拟仓冻结库存
    */
    @TableField("virtual_frozen_qty")
    private Integer virtualFrozenQty;
    /**
    * 平均库存（反推）
    */
    @TableField("back_avg_inventory_age")
    private BigDecimal backAvgInventoryAge;
    /**
    * 平均库龄（正推）
    */
    @TableField("avg_inventory_age")
    private BigDecimal avgInventoryAge;
    /**
    * 库龄计算差异
    */
    @TableField("is_diff")
    private Boolean isDiff;
    /**
    * 单据冻结数
    */
    @TableField("frozen_qty")
    private Integer frozenQty;
    /**
    * 冻结差异
    */
    @TableField("frozen_is_diff")
    private Boolean frozenIsDiff;
    /**
    * 统计日期
    */
    @TableField("date")
    private LocalDate date;


    public static final String SKU_ID = "sku_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String VIRTUAL_QTY = "virtual_qty";

    public static final String VIRTUAL_USABLE_QTY = "virtual_usable_qty";

    public static final String VIRTUAL_FROZEN_QTY = "virtual_frozen_qty";

    public static final String BACK_AVG_INVENTORY_AGE = "back_avg_inventory_age";

    public static final String AVG_INVENTORY_AGE = "avg_inventory_age";

    public static final String IS_DIFF = "is_diff";

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String FROZEN_IS_DIFF = "frozen_is_diff";

    public static final String DATE = "date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}