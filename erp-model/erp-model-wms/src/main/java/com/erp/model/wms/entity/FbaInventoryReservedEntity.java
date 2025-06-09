package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBA库存预留信息
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_inventory_reserved")
public class FbaInventoryReservedEntity extends BaseEntity<FbaInventoryReservedEntity> {

    /**
    * FBA库存id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 亚马逊FBA货件单号
    */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
    /**
    * 待调仓数量
    */
    @TableField("reserved_transfers_qty")
    private Integer reservedTransfersQty;
    /**
    * 调仓中数量
    */
    @TableField("reserved_processing_qty")
    private Integer reservedProcessingQty;
    /**
    * 买家订单数量
    */
    @TableField("reserved_order_qty")
    private Integer reservedOrderQty;


    public static final String MAIN_ID = "main_id";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

    public static final String RESERVED_TRANSFERS_QTY = "reserved_transfers_qty";

    public static final String RESERVED_PROCESSING_QTY = "reserved_processing_qty";

    public static final String RESERVED_ORDER_QTY = "reserved_order_qty";


}