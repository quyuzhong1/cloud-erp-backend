package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 物流单
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_bill")
public class LogisticsBillEntity extends BaseEntity<LogisticsBillEntity> {

    /**
    * 销售平台
    */
    @TableField("sales_platform")
    private String salesPlatform;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id 销售订单
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源code
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 出库id
    */
    @TableField("outstock_id")
    private String outstockId;
    /**
    * 出库code
    */
    @TableField("outstock_code")
    private String outstockCode;
    /**
    * 渠道id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 下单时间
    */
    @TableField("order_time")
    private LocalDateTime orderTime;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDate deliveryTime;
    /**
    * 运输单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
     * 目的地
     */
    @TableField("to_country")
    private String toCountry;

    public static final String SALES_PLATFORM = "sales_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String OUTSTOCK_ID = "outstock_id";

    public static final String OUTSTOCK_CODE = "outstock_code";

    public static final String CHANNEL_ID = "channel_id";

    public static final String ORDER_TIME = "order_time";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String TRANSPORT_NO = "transport_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}