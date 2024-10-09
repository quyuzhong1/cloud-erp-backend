package com.erp.model.oms.entity;

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
 * b2c退货订单
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_return")
public class SoB2cReturnEntity extends BaseEntity<SoB2cReturnEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 平台订单号
    */
    @TableField("platform_order_no")
    private String platformOrderNo;
    /**
    * 平台退货单号
    */
    @TableField("platform_return_no")
    private String platformReturnNo;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售订单编号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 订单金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 币别（原币）
    */
    @TableField("currency")
    private String currency;
    /**
    * 退货类型
    */
    @TableField("type")
    private String type;
    /**
    * 退货原因
    */
    @TableField("reason")
    private String reason;
    /**
    * 退货状态
    */
    @TableField("status")
    private String status;
    /**
    * 系统退货时间
    */
    @TableField("sys_return_time")
    private LocalDateTime sysReturnTime;


    public static final String CODE = "code";

    public static final String PLATFORM_ORDER_NO = "platform_order_no";

    public static final String PLATFORM_RETURN_NO = "platform_return_no";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String TYPE = "type";

    public static final String REASON = "reason";

    public static final String STATUS = "status";

    public static final String RETURN_INSTOCK_CODE = "return_instock_code";

    public static final String SYS_RETURN_TIME = "sys_return_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}