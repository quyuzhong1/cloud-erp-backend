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
 * 销售订单-tiktok全托管属性表
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_extend")
public class SoB2cExtendEntity extends BaseEntity<SoB2cExtendEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 要求发货时间
    */
    @TableField("required_delivery_time")
    private LocalDateTime requiredDeliveryTime;
    /**
    * 要求收货时间
    */
    @TableField("required_receive_time")
    private LocalDateTime requiredReceiveTime;
    /**
    * 发货预警时间
    */
    @TableField("delivery_warning_time")
    private LocalDateTime deliveryWarningTime;
    /**
    * 平台订单来源:PLATFORM=平台备货,MERCHANT=自主备货,ABNORMAL_REDELIVERY=异常补货  枚举：SoB2cExtendOrderSourceTypeEnum
    */
    @TableField("order_source_type")
    private String orderSourceType;
    /**
     * 预警次数
     */
    @TableField("warning_count")
    private Integer warningCount;
    /**
     * 分区id
     */
    @TableField("partition_id")
    private String partitionId;

    @TableField(exist = false)
    private String shopCountry;
    @TableField(exist = false)
    private String partitionCode;

    @TableField(exist = false)
    private String partitionName;

    public static final String MAIN_ID = "main_id";

    public static final String REQUIRED_DELIVERY_TIME = "required_delivery_time";

    public static final String REQUIRED_RECEIVE_TIME = "required_receive_time";

    public static final String DELIVERY_WARNING_TIME = "delivery_warning_time";

    public static final String ORDER_SOURCE_TYPE = "order_source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
