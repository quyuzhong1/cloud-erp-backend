package com.erp.model.oms.entity;

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
 * 多渠道订单主表
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_multi_channel")
public class SoMultiChannelEntity extends BaseEntity<SoMultiChannelEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 卖家订单编号/发货单号
    */
    @TableField("delivery_code")
    private String deliveryCode;
    /**
    * 发货平台
    */
    @TableField("delivery_platform")
    private String deliveryPlatform;
    /**
    * 销售平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
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
    * 货件编号
    */
    @TableField("shipment_code")
    private String shipmentCode;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售单据编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 订单状态
    */
    @TableField("bill_status")
    private String billStatus;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
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
    * 发货仓库id
    */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
    * 发货仓库名称
    */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
    * 配送条件
    */
    @TableField("shipping_method")
    private String shippingMethod;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
    */
    @TableField("abnormal_type")
    private String abnormalType;
    /**
    * 订单异常标示
    */
    @TableField("sign_order_error")
    private String signOrderError;
    /**
    * 订单备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 系统是否已出库
    */
    @TableField("has_outstock")
    private Boolean hasOutstock;
    /**
     * 创建状态(wait待创建,creating创建中,success创建成功,failed创建失败)
     */
    @TableField("create_status")
    private String createStatus;

    public static final String CODE = "code";

    public static final String DELIVERY_CODE = "delivery_code";

    public static final String DELIVERY_PLATFORM = "delivery_platform";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String SHIPMENT_CODE = "shipment_code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String TRACK_NO = "track_no";

    public static final String BILL_STATUS = "bill_status";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String SHIPPING_METHOD = "shipping_method";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String ABNORMAL_TYPE = "abnormal_type";

    public static final String SIGN_ORDER_ERROR = "sign_order_error";

    public static final String REMARK = "remark";

    public static final String HAS_OUTSTOCK = "has_outstock";

    @Override
    public Serializable pkVal() {
        return null;
    }

}