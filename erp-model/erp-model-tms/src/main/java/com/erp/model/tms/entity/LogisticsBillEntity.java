package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


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
     * 业务编号
     */
    @TableField("business_code")
    private String businessCode;
    /**
    * 渠道id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 下单时间
    */
    @TableField(value = "order_time", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime orderTime;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
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

    /**
     * 订单类型
     */
    @TableField("order_type")
    private String orderType;

    /**
     * 柜号
     */
    @TableField("counter_no")
    private String counterNo;

    /**
     * 船司/航司编码
     */
    @TableField("carrier_id")
    private String carrierId;
    /**
     * 发票状态
     */
    @TableField("invoices_status")
    private String invoicesStatus;

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
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 开船时间
     */
    @TableField("ship_time")
    private LocalDateTime shipTime;

    /**
     * 平台订单号
     */
    @TableField("platform_code")
    private String platformCode;

    /**
     * 发货类型(自发货、第三方仓、平台仓发货)
     */
    @TableField("shipment_type")
    private String shipmentType;

    /**
     * 买家电话
     */
    @TableField("tel_number")
    private String telNumber;

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

    public static void main(String[] args) {
        LocalDateTime dateTime1 = LocalDateTime.of(2023, 6, 24, 10, 30);
        LocalDateTime dateTime2 = LocalDateTime.of(2023, 6, 23, 10, 30);

        long daysBetween = ChronoUnit.DAYS.between(dateTime2, dateTime1);
        System.out.println("两个日期相差的天数: " + daysBetween);
    }

}