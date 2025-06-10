package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 物流渠道映射表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_mapping")
public class LogisticsMappingEntity extends BaseEntity<LogisticsMappingEntity> {

    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流平台
    */
    @TableField("sales_platform")
    private String salesPlatform;

    /**
     * 平台渠道id,type为platform 对应logistics_sale_channel,为warehouse对应logistics_channel的id
     */
    @TableField("platform_logistics_channel_id")
    private String platformLogisticsChannelId;

    /**
     * 标记发货订单类型（transportNo运单号、trackNo跟踪号）
     */
    @TableField("order_delivery_mark_type")
    private String orderDeliveryMarkType;

    /**
     * 承运商代号
     */
    @TableField("carrier_code")
    private String carrierCode;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
     * 类型,platform:平台，warehouse:仓库
     */
    @TableField("type")
    private String type;

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String SALES_PLATFORM = "sales_platform";


}