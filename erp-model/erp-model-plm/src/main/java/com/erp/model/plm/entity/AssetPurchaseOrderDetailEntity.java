package com.erp.model.plm.entity;

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
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_purchase_order_detail")
public class AssetPurchaseOrderDetailEntity extends BaseEntity<AssetPurchaseOrderDetailEntity> {

    /**
    * 资产采购单单头id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 资产id
    */
    @TableField("asset_id")
    private String assetId;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 资产名称
    */
    @TableField("asset_name")
    private String assetName;
    /**
    * 含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;
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
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 采购数量
    */
    @TableField("purchase_qty")
    private BigDecimal purchaseQty;
    /**
    * 价税合计
    */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    /**
    * 计划交期
    */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;
    /**
    * 是否加急
    */
    @TableField("is_urgent")
    private Boolean isUrgent;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 金蝶明细id
    */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;
    /**
    * 结束收货AssetPurchaseOrderReceiveEnum
    */
    @TableField("end_receive")
    private String endReceive;
    /**
    * 结束验收时间
    */
    @TableField("end_receive_time")
    private LocalDateTime endReceiveTime;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 标识(首套模，复制模)
     */
    @TableField("tag")
    private String tag;


    public static final String MAIN_ID = "main_id";

    public static final String ASSET_ID = "asset_id";

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_NAME = "asset_name";

    public static final String TAX_PRICE = "tax_price";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String TAX_RATE = "tax_rate";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String TOTAL_AMOUNT = "total_amount";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String IS_URGENT = "is_urgent";

    public static final String REMARK = "remark";

    public static final String KINGDEE_DETAIL_ID = "kingdee_detail_id";

    public static final String END_RECEIVE = "end_receive";

    public static final String END_RECEIVE_TIME = "end_receive_time";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}