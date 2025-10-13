package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * <p>
 * B2C销售订单物流信息表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_logistics")
public class SoB2cLogisticsEntity extends BaseEntity<SoB2cLogisticsEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 物流单号 (运单号)
     */
    @TableField("code")
    private String code;

    /**
     * 物流跟踪号
     */
    @TableField("track_no")
    private String trackNo;
    /**
     * 买家自选物流名称
     */
    @TableField("name")
    private String name;
    /**
     * 物流渠道名称
     */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;

    /**
     * 物流渠道id
     */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;

    /**
     * 发货时间
     */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
     * 预估运费
     */
    @TableField("estimated_shipping_cost")
    private BigDecimal estimatedShippingCost;
    /**
     * 预估运费币别
     */
    @TableField("estimated_shipping_currency")
    private String estimatedShippingCurrency;
    /**
     * 实际运费
     */
    @TableField("actual_shipping_cost")
    private BigDecimal actualShippingCost;
    /**
     * 实际运费币别
     */
    @TableField("actual_shipping_currency")
    private String actualShippingCurrency;
    /**
     * 包装重量
     */
    @TableField("weight")
    private BigDecimal weight;
    /**
     * 包装辅料skuId
     */
    @TableField("accessories_sku_id")
    private String accessoriesSkuId;
    /**
     * 包装辅料sku编码
     */
    @TableField("accessories_sku_no")
    private String accessoriesSkuNo;
    /**
     * 包装辅料数量
     */
    @TableField("accessories_qty")
    private Integer accessoriesQty;
    /**
     * 包装辅料净重
     */
    @TableField("accessories_nw")
    private BigDecimal accessoriesNw;
    /**
     * 包装辅料费
     */
    @TableField("accessories_cost")
    private BigDecimal accessoriesCost;
    /**
     * 包装辅料费币别
     */
    @TableField("accessories_cost_currency")
    private String accessoriesCostCurrency;
    /**
     * 长
     */
    @TableField("length")
    private BigDecimal length;
    /**
     * 宽
     */
    @TableField("width")
    private BigDecimal width;
    /**
     * 高
     */
    @TableField("height")
    private BigDecimal height;
    /**
     * 物流类型
     */
    @TableField("logistic_type")
    private String logisticType;
    /**
     * 中转物流商id
     */
    @TableField("transfer_logistics_supplier_id")
    private String transferLogisticsSupplierId;

    /**
     * 中转商渠道id
     */
    @TableField("transfer_logistics_channel_id")
    private String transferLogisticsChannelId;

    @TableField("source_system")
    private String sourceSystem;
    /**
     * ioss税号
     */
    @TableField("ioss_tax_no")
    private String iossTaxNo;
    /**
     * 申报组织ID（sys_accounting_company.id）
     */
    @TableField("declare_org_id")
    private String declareOrgId;

    public static final String MAIN_ID = "main_id";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String logistics_channel_name = "logistics_channel_name";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String ESTIMATED_SHIPPING_COST = "estimated_shipping_cost";

    public static final String ESTIMATED_SHIPPING_CURRENCY = "estimated_shipping_currency";

    public static final String ACTUAL_SHIPPING_COST = "actual_shipping_cost";

    public static final String ACTUAL_SHIPPING_CURRENCY = "actual_shipping_currency";

    public static final String WEIGHT = "weight";

    public static final String ACCESSORIES_SKU_ID = "accessories_sku_id";

    public static final String ACCESSORIES_SKU_NO = "accessories_sku_no";

    public static final String ACCESSORIES_QTY = "accessories_qty";

    public static final String ACCESSORIES_NW = "accessories_nw";

    public static final String ACCESSORIES_COST = "accessories_cost";

    public static final String ACCESSORIES_COST_CURRENCY = "accessories_cost_currency";

    public static final String LENGTH = "length";

    public static final String WIDTH = "width";

    public static final String HEIGHT = "height";


}