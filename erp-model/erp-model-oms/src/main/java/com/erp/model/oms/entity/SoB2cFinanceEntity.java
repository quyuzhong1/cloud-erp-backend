package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * B2C销售订单财务信息表
 * </p>
 *
 * @author will
 * @since 2023-09-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_finance")
public class SoB2cFinanceEntity extends BaseEntity<SoB2cFinanceEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 运费收入
    */
    @TableField("shipping_cost")
    private BigDecimal shippingCost;
    /**
    * 商品成本
    */
    @TableField("item_cost")
    private BigDecimal itemCost;
    /**
    * 物流成本
    */
    @TableField("logistics_cost")
    private BigDecimal logisticsCost;
    /**
    * 平台费
    */
    @TableField("platform_cost")
    private BigDecimal platformCost;
    /**
    * 转账费
    */
    @TableField("transfer_cost")
    private BigDecimal transferCost;
    /**
    * 包装辅料费
    */
    @TableField("accessories_cost")
    private BigDecimal accessoriesCost;
    /**
    * VAT税费
    */
    @TableField("vat_cost")
    private BigDecimal vatCost;


    public static final String MAIN_ID = "main_id";

    public static final String CURRENCY = "currency";

    public static final String SHIPPING_COST = "shipping_cost";

    public static final String ITEM_COST = "item_cost";

    public static final String LOGISTICS_COST = "logistics_cost";

    public static final String PLATFORM_COST = "platform_cost";

    public static final String TRANSFER_COST = "transfer_cost";

    public static final String ACCESSORIES_COST = "accessories_cost";

    public static final String VAT_COST = "vat_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}