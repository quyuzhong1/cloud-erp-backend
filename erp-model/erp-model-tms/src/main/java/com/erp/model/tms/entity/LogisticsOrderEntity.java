package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 物流下单表
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("logistics_order")
public class LogisticsOrderEntity extends BaseEntity<LogisticsOrderEntity> {

    /**
    * 售后申请id,after_sale.id
    */
    @TableField("after_sale_id")
    private String afterSaleId;
    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态
    */
    @TableField("status")
    private String status;
    /**
    * 面单状态
    */
    @TableField("label_status")
    private String labelStatus;
    /**
    * 异常类型
    */
    @TableField("exception_type")
    private String exceptionType;
    /**
    * 异常原因
    */
    @TableField("exception_reason")
    private String exceptionReason;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源单据类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
     * 物流平台
     */
    @TableField("logistics_platform")
    private String logisticsPlatform;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 收件人
    */
    @TableField("receiver")
    private String receiver;
    /**
    * 电话
    */
    @TableField("contact_number")
    private String contactNumber;
    /**
    * 国家,dict_country.id
    */
    @TableField("country")
    private String country;
    /**
    * 省/州
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 详细地址
    */
    @TableField("detailed_address")
    private String detailedAddress;
    /**
     * 顺丰下单时用的唯一id
     */
    @TableField("order_id")
    private String orderId;

    public static final String AFTER_SALE_ID = "after_sale_id";

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String LABEL_STATUS = "label_status";

    public static final String EXCEPTION_TYPE = "exception_type";

    public static final String EXCEPTION_REASON = "exception_reason";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String TRACK_NO = "track_no";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String REMARK = "remark";

    public static final String RECEIVER = "receiver";

    public static final String CONTACT_NUMBER = "contact_number";

    public static final String COUNTRY = "country";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DETAILED_ADDRESS = "detailed_address";

    public static final String ORDER_ID = "order_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}