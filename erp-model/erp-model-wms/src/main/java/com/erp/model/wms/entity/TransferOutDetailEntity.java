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
 * 分布式调出单明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_out_detail")
public class TransferOutDetailEntity extends BaseEntity<TransferOutDetailEntity> {

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
     * 调出数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 调出仓位
     */
    @TableField("out_warehouse_location")
    private String outWarehouseLocation;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    

    public static final String OUT_WAREHOUSE_LOCATION = "out_warehouse_location";

    

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}
