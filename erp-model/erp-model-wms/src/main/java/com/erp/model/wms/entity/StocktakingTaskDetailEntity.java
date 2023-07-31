package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    public static final String QTY = "qty";

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String DIFF_QTY = "diff_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
