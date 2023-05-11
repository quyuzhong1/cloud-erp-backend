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
 * 分布式调入单
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_in_detail")
public class TransferInDetailEntity extends BaseEntity<TransferInDetailEntity> {

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
     * 调入数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 调出仓位
     */
    @TableField("out_warehouse_location")
    private String outWarehouseLocation;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 计划调入数量
     */
    @TableField("plan_qty")
    private Integer planQty;

    /**
     * 调入仓位
     */
    @TableField("in_warehouse_location")
    private String inWarehouseLocation;

    /**
     * 途损数
     */
    @TableField("transit_damage_qty")
    private Integer transitDamageQty;

    /**
     * 途损 责任方
     */
    @TableField("transit_damage_responsible")
    private String transitDamageResponsible;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String OUT_WAREHOUSE_LOCATION = "out_warehouse_location";

    public static final String REMARK = "remark";

    public static final String PLAN_QTY = "plan_qty";

    public static final String IN_WAREHOUSE_LOCATION = "in_warehouse_location";

    public static final String TRANSIT_DAMAGE_QTY = "transit_damage_qty";

    public static final String TRANSIT_DAMAGE_RESPONSIBLE = "transit_damage_responsible";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
