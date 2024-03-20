package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * <p>
 * 头程物流单
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_first_mile_logistic")
public class TmsFirstMileLogisticEntity extends BaseEntity<TmsFirstMileLogisticEntity> {

    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 柜号
    */
    @TableField("counter_no")
    private String counterNo;
    /**
    * 对账状态
    */
    @TableField("reconciliation_status")
    private String reconciliationStatus;
    /**
    * 发票状态
    */
    @TableField("invoices_status")
    private String invoicesStatus;
    /**
    * 物流状态
    */
    @TableField("logistics_status")
    private String logisticsStatus;
    /**
    * 运输方式
    */
    @TableField("shipping_method")
    private String shippingMethod;
    /**
    * 物流商id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 物流下单时间
    */
    @TableField("logistics_order_time")
    private LocalDateTime logisticsOrderTime;
    /**
    * 开船时间
    */
    @TableField("ship_time")
    private LocalDateTime shipTime;
    /**
    * 运输时间
    */
    @TableField("transit_time")
    private LocalDateTime transitTime;
    /**
    * 到达时间
    */
    @TableField("arrival_time")
    private LocalDateTime arrivalTime;
    /**
    * 签收时间
    */
    @TableField("sign_time")
    private LocalDateTime signTime;
    /**
    * 实际重量
    */
    @TableField("actual_weight")
    private BigDecimal actualWeight;
    /**
    * 实际重量单位
    */
    @TableField("actual_weight_unit")
    private String actualWeightUnit;
    /**
    * 实际体积重
    */
    @TableField("actual_volume_weight")
    private BigDecimal actualVolumeWeight;
    /**
    * 实际体积中单位
    */
    @TableField("actual_volume_weight_unit")
    private String actualVolumeWeightUnit;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率表id
    */
    @TableField("exchange_rate_id")
    private String exchangeRateId;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private String exchangeRate;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源code
    */
    @TableField("source_code")
    private String sourceCode;

    public static final String TRANSPORT_NO = "transport_no";

    public static final String COUNTER_NO = "counter_no";

    public static final String RECONCILIATION_STATUS = "reconciliation_status";

    public static final String INVOICES_STATUS = "invoices_status";

    public static final String LOGISTICS_STATUS = "logistics_status";

    public static final String SHIPPING_METHOD = "shipping_method";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String REMARK = "remark";

    public static final String LOGISTICS_ORDER_TIME = "logistics_order_time";

    public static final String SHIP_TIME = "ship_time";

    public static final String TRANSIT_TIME = "transit_time";

    public static final String ARRIVAL_TIME = "arrival_time";

    public static final String SIGN_TIME = "sign_time";

    public static final String ACTUAL_WEIGHT = "actual_weight";

    public static final String ACTUAL_WEIGHT_UNIT = "actual_weight_unit";

    public static final String ACTUAL_VOLUME_WEIGHT = "actual_volume_weight";

    public static final String ACTUAL_VOLUME_WEIGHT_UNIT = "actual_volume_weight_unit";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE_ID = "exchange_rate_id";

    public static final String EXCHANGE_RATE = "exchange_rate";

    @Override
    public Serializable pkVal() {
        return null;
    }

}