package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


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
     * 销售渠道表id(logistics_sale_channel)
     */
    @TableField("logistics_sale_channel_id")
    private String logisticsSaleChannelId;

    /**
     * 标发订单类型（运单号、跟踪号）
     */
    @TableField("standard_order_type")
    private String standardOrderType;


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String SALES_PLATFORM = "sales_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}