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
 * 销售订单出库明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_outstock_detail")
public class SoOutstockDetailEntity extends BaseEntity<SoOutstockDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

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
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 是否关闭 
     */
    @TableField("is_close")
    private Boolean isClose;

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

    @TableField(exist = false)
    private String approveStatus;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLAN_QTY = "plan_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String IS_CLOSE = "is_close";

    public static final String REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
