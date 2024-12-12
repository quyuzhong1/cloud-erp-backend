package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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
@TableName("virtual_trans_flow_detail")
public class VirtualTransFlowDetailEntity extends BaseEntity<VirtualTransFlowDetailEntity> {

    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
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