package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 销售订单详情
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_detail")
public class SoDetailEntity extends BaseEntity<SoDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 销售数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 发货状态
     * unShipped 未发货
     * partialShipment 部分发货
     */
    @TableField("delivery_status")
    private DeliveryStatusEnum deliveryStatus;

    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 销售金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 是否赠品 true 是
     */
    @TableField("is_gift")
    private Boolean isGift;

    /**
     * 是否补发 true 是
     */
    @TableField("is_reissue")
    private Boolean isReissue;

    /**
     * 是否关闭 true 是
     */
    @TableField("is_close")
    private Boolean isClose;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    @TableField(exist = false)
    private String ApproveStatus;




    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String PRICE = "price";

    public static final String TAX_RATE = "tax_rate";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String IS_GIFT = "is_gift";

    public static final String IS_REISSUE = "is_reissue";

    public static final String IS_CLOSE = "is_close";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
