package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 速卖通发货单
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("aliexpress_delivery")
public class AliexpressDeliveryEntity extends BaseEntity<AliexpressDeliveryEntity> {

    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 销售单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售单编号
    */
    @TableField("so_code")
    private String soCode;
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
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 订单创建时间
    */
    @TableField("trade_create_time")
    private LocalDateTime tradeCreateTime;
    /**
    * 订单出库时间
    */
    @TableField("out_bound_time")
    private LocalDateTime outBoundTime;
    /**
    * 平台发货仓库
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
     * 平台发货状态
     * AliexpressDeliveryOrderStatusEnum
     */
    @TableField("order_status")
    private String orderStatus;


    public static final String PLATFORM_CODE = "platform_code";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String TRACK_NO = "track_no";

    public static final String TRADE_CREATE_TIME = "trade_create_time";

    public static final String OUT_BOUND_TIME = "out_bound_time";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}