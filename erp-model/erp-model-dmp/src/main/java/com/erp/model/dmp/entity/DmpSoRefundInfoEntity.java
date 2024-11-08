package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台销售退款单主表
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_refund_info")
public class DmpSoRefundInfoEntity extends BaseEntity<DmpSoRefundInfoEntity> {

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
    * 退款时间
    */
    @TableField("refund_time")
    private LocalDateTime refundTime;
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
    * 单据编号（唯一）
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 销售平台原始单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 退款原因
    */
    @TableField("reason")
    private String reason;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 退款状态：1、成功 2、失败 3、作废
    */
    @TableField("status")
    private String status;
    /**
    * 平台原始状态
    */
    @TableField("platform_original_status")
    private String platformOriginalStatus;
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
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 买家账号
    */
    @TableField("buyer_user_id")
    private String buyerUserId;
    /**
    * 买家姓名
    */
    @TableField("buyer_name")
    private String buyerName;
    /**
    * 币别
    */
    @TableField("currency_code")
    private String currencyCode;
    /**
    * 汇率
    */
    @TableField("currency_rate")
    private BigDecimal currencyRate;
    /**
    * 退货金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 运费
    */
    @TableField("shipping_cost")
    private BigDecimal shippingCost;
    @TableField("delivery_time")
    private LocalDate deliveryTime;
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
     * 来源ID:shopify=dmp_so_info主键id
     */
    @TableField("source_id")
    private String sourceId;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String REFUND_TIME = "refund_time";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String REASON = "reason";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";

    public static final String PLATFORM_ORIGINAL_STATUS = "platform_original_status";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String COUNTRY = "country";

    public static final String BUYER_USER_ID = "buyer_user_id";

    public static final String BUYER_NAME = "buyer_name";

    public static final String CURRENCY_CODE = "currency_code";

    public static final String CURRENCY_RATE = "currency_rate";

    public static final String AMOUNT = "amount";

    public static final String SHIPPING_COST = "shipping_cost";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}