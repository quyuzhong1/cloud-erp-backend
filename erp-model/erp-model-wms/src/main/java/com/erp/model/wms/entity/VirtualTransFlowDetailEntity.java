package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 虚拟仓库存流水明细
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("virtual_trans_flow_detail")
public class VirtualTransFlowDetailEntity extends BaseEntity<VirtualTransFlowDetailEntity> {

    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 后数量
    */
    @TableField("cur_inventory_qty")
    private Integer curInventoryQty;
    /**
    * 虚拟仓流水id
    */
    @TableField("virtual_trans_flow_id")
    private String virtualTransFlowId;
    /**
    * 虚拟仓库存明细id
    */
    @TableField("virtual_inventory_detail_id")
    private String virtualInventoryDetailId;
    /**
    * 操作时间
    */
    @TableField("trade_time")
    private LocalDateTime tradeTime;

    public VirtualTransFlowDetailEntity(String id, Integer afterQty) {
        super(id);
        this.curInventoryQty = afterQty;
    }


    public static final String QTY = "qty";

    public static final String CUR_INVENTORY_QTY = "cur_inventory_qty";

    public static final String VIRTUAL_TRANS_FLOW_ID = "virtual_trans_flow_id";

    public static final String VIRTUAL_INVENTORY_DETAIL_ID = "virtual_inventory_detail_id";

    public static final String TRADE_TIME = "trade_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}