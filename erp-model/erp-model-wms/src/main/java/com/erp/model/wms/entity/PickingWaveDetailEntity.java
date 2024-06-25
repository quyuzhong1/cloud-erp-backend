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
@TableName("picking_wave_detail")
public class PickingWaveDetailEntity extends BaseEntity<PickingWaveDetailEntity> implements Serializable {
    /**
     * 包裹号
     */
    @TableField("basket_no")
    private String basketNo;

    /**
     * 销售订单编号
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 发货单号
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
     * 销售数量
     */
    @TableField("sales_qty")
    private String salesQty;
    /**
     * 异常原因
     */
    @TableField("abnormal_cause")
    private String abnormalCause;
}
