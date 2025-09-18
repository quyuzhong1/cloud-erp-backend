package com.erp.model.dmp.entity;

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
 * 
 * </p>
 *
 * @author zdy
 * @since 2025-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_platform_so_delivery")
public class DmpPlatformSoDeliveryEntity extends BaseEntity<DmpPlatformSoDeliveryEntity> {

    /**
    * 卖家订单编号
    */
    @TableField("code")
    private String code;
    /**
    * 亚马逊订单编号

    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 发货时间（东八区）
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 订单类型（soMultiChannel多渠道）
    */
    @TableField("order_type")
    private String orderType;
    /**
    * 货件id
    */
    @TableField("shipment_id")
    private String shipmentId;
    /**
     * 订单状态
     */
    @TableField("order_status")
    private String orderStatus;
    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 亚马逊账号代号
     */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 店铺ID
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 任务来源唯一加密代号
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 任务数据加密代号
     */
    @TableField("data_encrypt")
    private String dataEncrypt;

    public static final String CODE = "code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String TRACK_NO = "track_no";

    public static final String ORDER_TYPE = "order_type";

    public static final String SHIPMENT_ID = "shipment_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}