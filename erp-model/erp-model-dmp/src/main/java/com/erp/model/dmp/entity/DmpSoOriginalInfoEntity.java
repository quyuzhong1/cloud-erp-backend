package com.erp.model.dmp.entity;

import java.math.BigDecimal;
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
 * 中台原始销售订单表
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_original_info")
public class DmpSoOriginalInfoEntity extends BaseEntity<DmpSoOriginalInfoEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 第三方单据编号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 销售平台原始单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 店铺编号
    */
    @TableField("shop_no")
    private String shopNo;
    /**
    * 平台状态
    */
    @TableField("trade_status")
    private String tradeStatus;
    /**
    * 支付状态
    */
    @TableField("pay_status")
    private String payStatus;
    /**
    * 下单时间
    */
    @TableField("trade_time")
    private LocalDateTime tradeTime;
    /**
    * 支付时间
    */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
    * 买家留言
    */
    @TableField("buyer_message")
    private String buyerMessage;
    /**
    * 客服备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 邮箱
    */
    @TableField("buyer_email")
    private String buyerEmail;
    /**
    * 买家姓名
    */
    @TableField("buyer_name")
    private String buyerName;
    /**
    * 收件人姓名
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 收件人国家
    */
    @TableField("receiver_country")
    private String receiverCountry;
    /**
    * 收件人省份
    */
    @TableField("receiver_province")
    private String receiverProvince;
    /**
    * 市
    */
    @TableField("receiver_city")
    private String receiverCity;
    /**
    * 区
    */
    @TableField("receiver_district")
    private String receiverDistrict;
    /**
    * 收件人地址
    */
    @TableField("receiver_address")
    private String receiverAddress;
    /**
    * 收件人手机
    */
    @TableField("receiver_mobile")
    private String receiverMobile;
    /**
    * 收件人电话
    */
    @TableField("receiver_telno")
    private String receiverTelno;
    /**
    * 优惠
    */
    @TableField("discount")
    private BigDecimal discount;
    /**
    * 买家已付金额
    */
    @TableField("paid")
    private BigDecimal paid;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 退款金额
    */
    @TableField("refund_amount")
    private BigDecimal refundAmount;
    /**
    * 订单来源
    */
    @TableField("trade_from")
    private String tradeFrom;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String SHOP_NO = "shop_no";

    public static final String TRADE_STATUS = "trade_status";

    public static final String PAY_STATUS = "pay_status";

    public static final String TRADE_TIME = "trade_time";

    public static final String PAY_TIME = "pay_time";

    public static final String BUYER_MESSAGE = "buyer_message";

    public static final String REMARK = "remark";

    public static final String BUYER_EMAIL = "buyer_email";

    public static final String BUYER_NAME = "buyer_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVER_COUNTRY = "receiver_country";

    public static final String RECEIVER_PROVINCE = "receiver_province";

    public static final String RECEIVER_CITY = "receiver_city";

    public static final String RECEIVER_DISTRICT = "receiver_district";

    public static final String RECEIVER_ADDRESS = "receiver_address";

    public static final String RECEIVER_MOBILE = "receiver_mobile";

    public static final String RECEIVER_TELNO = "receiver_telno";

    public static final String DISCOUNT = "discount";

    public static final String PAID = "paid";

    public static final String CURRENCY = "currency";

    public static final String REFUND_AMOUNT = "refund_amount";

    public static final String TRADE_FROM = "trade_from";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}