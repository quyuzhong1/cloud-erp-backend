package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * b2b发货拦截单详情
 * </p>
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2b_delivery_intercept_detail")
public class SoB2bDeliveryInterceptDetailEntity extends BaseEntity<SoB2bDeliveryInterceptDetailEntity> {

    @TableField("main_id")
    private String mainId;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_no")
    private String skuNo;

    @TableField("delivery_qty")
    private Integer deliveryQty;

    @TableField("warehouse_id")
    private String warehouseId;

    @TableField("warehouse_name")
    private String warehouseName;

    @TableField("warehouse_location")
    private String warehouseLocation;

    @TableField("source_detail_id")
    private String sourceDetailId;

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
