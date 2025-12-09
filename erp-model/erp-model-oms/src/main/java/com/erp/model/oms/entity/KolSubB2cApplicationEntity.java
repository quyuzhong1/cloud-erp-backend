package com.erp.model.oms.entity;

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
 * B2C寄样申请单拆分单
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_sub_b2c_application")
public class KolSubB2cApplicationEntity extends BaseEntity<KolSubB2cApplicationEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 拆分单编码
    */
    @TableField("code")
    private String code;
    /**
    * 平台销售单号
    */
    @TableField("platform_so_code")
    private String platformSoCode;
    /**
    * 平台id  DmpBasicSystemCodeEnum
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 达人id
    */
    @TableField("partner_id")
    private String partnerId;
    /**
    * 达人昵称
    */
    @TableField("nickname")
    private String nickname;
    /**
    * 发货状态：waitShipped=待发货,shipped=已发货,partialShipped=部分发货  枚举：KolSubB2cApplicationDeliveryStatusEnum
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 订单状态：not=未生成,notApprove=未审核,approve=已审核  枚举：KolSubB2cApplicationOrderStatusEnum
    */
    @TableField("order_status")
    private String orderStatus;
    /**
    * 跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 平台订单id
    */
    @TableField("platform_order_id")
    private String platformOrderId;
    /**
    * 平台订单编码
    */
    @TableField("platform_order_code")
    private String platformOrderCode;


    public static final String REMARK = "remark";

    public static final String SOURCE_ID = "source_id";

    public static final String CODE = "code";

    public static final String PLATFORM_SO_CODE = "platform_so_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String PARTNER_ID = "partner_id";

    public static final String NICKNAME = "nickname";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String ORDER_STATUS = "order_status";

    public static final String TRACK_NO = "track_no";

    public static final String PLATFORM_ORDER_ID = "platform_order_id";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
