package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 仓库签收明细单
 * </p>
 *
 * @author LUO_WG
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

    @TableField(exist = false)
    private String approveUserName;

    @TableField(exist = false)
    private LocalDateTime approveTime;

    @TableField(exist = false)
    private String approveStatus;

    public WarehouseReceiveDetailEntity() {
        this.receiveQty = 0;
        this.exceedQty = 0;
    }
}
