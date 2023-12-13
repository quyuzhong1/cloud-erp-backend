package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * b2c发货单详情
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_delivery_detail")
public class SoB2cDeliveryDetailEntity extends BaseEntity<SoB2cDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
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
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;
    /**
    * 待扫描数量
    */
    @TableField("wait_scan_qty")
    private Integer waitScanQty;
    /**
    * 来源详情id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String WAIT_SCAN_QTY = "wait_scan_qty";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}