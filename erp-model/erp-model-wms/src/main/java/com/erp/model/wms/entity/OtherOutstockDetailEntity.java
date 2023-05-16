package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 其他出库明细表
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("other_outstock_detail")
public class OtherOutstockDetailEntity extends BaseEntity<OtherOutstockDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 应发数量
     */
    @TableField("plan_qty")
    private Integer planQty;

    /**
     * 实发数量
     */
    @TableField("actual_qty")
    private Integer actualQty;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLAN_QTY = "plan_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String UNIT = "unit";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
