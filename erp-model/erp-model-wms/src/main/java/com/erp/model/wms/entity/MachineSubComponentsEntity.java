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
 * 加工单子件明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("machine_sub_components")
public class MachineSubComponentsEntity extends BaseEntity<MachineSubComponentsEntity> {

    /**
     * 加工单明细id
     */
    @TableField("detail_id")
    private String detailId;

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
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 收货仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 收货仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

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

    /**
     * 是否是子件子级（作用用于前端判断）
     */
    @TableField("is_child")
    private Boolean isChild;



    public static final String DETAIL_ID = "detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String UNIT = "unit";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String CUR_INVENTORY_QTY = "cur_inventory_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
