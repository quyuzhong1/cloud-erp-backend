package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 仓库签收明细单
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("po_receive_detail")
public class WarehouseReceiveDetailEntity extends BaseEntity<WarehouseReceiveDetailEntity> {

    /**
     * 签收单主表id
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
     * 计划交货时间
     */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;

    /**
     * 收货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 超收数量
     */
    @TableField("exceed_qty")
    private Integer exceedQty;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 采购订单明细表id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

    /**
     * 金蝶明细id
     */
    @TableField("Kingdee_detail_id")
    private String kingdeeDetailId;

    /**
     * 明细来源id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    @TableField(exist = false)
    private String approveUserName;

    @TableField(exist = false)
    private LocalDateTime approveTime;

    @TableField(exist = false)
    private String approveStatus;

    @TableField(exist = false)
    private LocalDate billDate;

    /**
     * 入库状态（0未入库，1部分入库，2已入库）
     */
    @TableField("in_stock_status")
    private String inStockStatus;
}
