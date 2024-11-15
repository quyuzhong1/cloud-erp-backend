package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 组包预报详情
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("package_forecast_detail")
public class PackageForecastDetailEntity extends BaseEntity<PackageForecastDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 销售订单code
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道名
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 运输单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 重量
    */
    @TableField("weight")
    private BigDecimal weight;
    /**
    * 重量单位 
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 交接状态
    */
    @TableField("handover_status")
    private String handoverStatus;


    /**
     * 来源code 目前存速卖通物流单详情里面的 outOrderCode
     */
    @TableField("source_code")
    private String sourceCode;


    public static final String MAIN_ID = "main_id";

    public static final String SO_CODE = "so_code";

    public static final String SO_ID = "so_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRACK_NO = "track_no";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String HANDOVER_STATUS = "handover_status";



    @Override
    public Serializable pkVal() {
        return null;
    }

}