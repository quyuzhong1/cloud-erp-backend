package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 拣货波次明细数据表实体
 * @date 2024-06-24
 * @author tanmujin
 */
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wave_list_detail")
public class WaveListDetailEntity extends BaseEntity<WaveListDetailEntity> implements Serializable {
    /**
     * 波次id
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 包裹号
     */
    @TableField("basket_no")
    private String basketNo;

    /**
     * 销售订单id
     */
    @TableField("so_id")
    private String soId;
    /**
     * 销售订单编号
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 发货id
     */
    @TableField("delivery_id")
    private String deliveryId;
    /**
     * 发货单号
     */
    @TableField("delivery_code")
    private String deliveryCode;

    /**
     * 拣货状态
     */
    @TableField("picking_status")
    private String pickingStatus;

    /**
     * 物流渠道
     */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
}


