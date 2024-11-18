package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 期初头程分摊明细
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("init_first_mile_allocation_detail")
public class InitFirstMileAllocationDetailEntity extends BaseEntity<InitFirstMileAllocationDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 发货单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 发货单明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 发货单编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 数据来源类型：firstMileDelivery=头程发货单
     * SourceTypeEnum
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 业务单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务来源类型：FBA=FBA，第三方仓=thirdWarehouse
    */
    @TableField("business_type")
    private String businessType;
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
    * 仓库ID
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
     * 平台SKU
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 上线前签收数量
    */
    @TableField("init_receive_qty")
    private Integer initReceiveQty;
    /**
    * 期初在途头程费用
    */
    @TableField("init_transit_cost")
    private BigDecimal initTransitCost;
    /**
     * 期初在途头程关税
     */
    @TableField("init_transit_Tariff")
    private BigDecimal initTransitTariff;
    /**
     * 期初暂估头程费用
     */
    @TableField("init_estimated_cost")
    private BigDecimal initEstimatedCost;
    /**
    * 期初暂估头程关税
    */
    @TableField("init_estimated_tariff")
    private BigDecimal initEstimatedTariff;
    /**
    * 分摊重量
    */
    @TableField("weight_allocation")
    private BigDecimal weightAllocation;
    /**
    * 产品成本
    */
    @TableField("product_cost")
    private BigDecimal productCost;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 币别符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String INIT_RECEIVE_QTY = "init_receive_qty";

    public static final String INIT_TRANSIT_COST = "init_transit_cost";

    public static final String INIT_ESTIMATED_TARIFF = "init_estimated_tariff";

    public static final String WEIGHT_ALLOCATION = "weight_allocation";

    public static final String PRODUCT_COST = "product_cost";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String FIELD_CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    @Override
    public Serializable pkVal() {
        return null;
    }

}