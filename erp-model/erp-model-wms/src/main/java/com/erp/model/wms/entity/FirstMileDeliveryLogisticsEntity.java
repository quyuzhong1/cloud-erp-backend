package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 头程发货单物流信息表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_delivery_logistics")
public class FirstMileDeliveryLogisticsEntity extends BaseEntity<FirstMileDeliveryLogisticsEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 发货单号
    */
    @TableField("delivery_code")
    private String deliveryCode;
    /**
    * 物流方式
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
    * 物流渠道
    */
    @TableField("logistics_channel")
    private String logisticsChannel;
    /**
    * 发货时间
    */
    @TableField(value = "delivery_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime deliveryTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 物流跟踪号
    */
    @TableField(exist = false)
    private List<String> trackingNoList;

    public static final String MAIN_ID = "main_id";

    public static final String DELIVERY_CODE = "delivery_code";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_CHANNEL = "logistics_channel";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String TRACKING_NO = "tracking_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}