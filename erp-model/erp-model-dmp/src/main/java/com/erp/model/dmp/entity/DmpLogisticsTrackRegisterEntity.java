package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 物流注册表
 * </p>
 *
 * @author zdy
 * @since 2024-11-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_logistics_track_register")
public class DmpLogisticsTrackRegisterEntity extends BaseEntity<DmpLogisticsTrackRegisterEntity> {

    /**
    * 跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 轨迹查询类型（运单号transportNo跟踪号trackNo）
    */
    @TableField("track_query_type")
    private String trackQueryType;
    /**
    * 渠道id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 平台订单号
    */
    @TableField("platform_order_no")
    private String platformOrderNo;
    /**
    * 船司/航司
    */
    @TableField("carrier_id")
    private String carrierId;
    /**
    * 电话
    */
    @TableField("tel_number")
    private String telNumber;


    public static final String TRACK_NO = "track_no";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String TRACK_QUERY_TYPE = "track_query_type";

    public static final String CHANNEL_ID = "channel_id";

    public static final String PLATFORM_ORDER_NO = "platform_order_no";

    public static final String CARRIER_ID = "carrier_id";

    public static final String TEL_NUMBER = "tel_number";

    @Override
    public Serializable pkVal() {
        return null;
    }

}