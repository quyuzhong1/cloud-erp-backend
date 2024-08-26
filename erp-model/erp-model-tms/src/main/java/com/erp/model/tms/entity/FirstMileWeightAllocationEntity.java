package com.erp.model.tms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 头程重量分摊
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_weight_allocation")
public class FirstMileWeightAllocationEntity extends BaseEntity<FirstMileWeightAllocationEntity> {

    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 发货单明细id
    */
    @TableField("delivery_detail_id")
    private String deliveryDetailId;
    /**
    * 业务单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 物流运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 费用分摊状态
    */
    @Deprecated
    @TableField("allocation_status")
    private String allocationStatus;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台skuId
    */
    @TableField("platform_sku_id")
    private String platformSkuId;
    /**
    * 平台skuNo
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 箱ID
    */
    @TableField("box_id")
    private String boxId;
    /**
    * 箱号
    */
    @TableField("box_no")
    private String boxNo;
    /**
    * 发货量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 箱长
    */
    @TableField("box_length")
    private BigDecimal boxLength;
    /**
    * 箱宽
    */
    @TableField("box_width")
    private BigDecimal boxWidth;
    /**
    * 箱高
    */
    @TableField("box_height")
    private BigDecimal boxHeight;
    /**
    * 箱子尺寸单位
    */
    @TableField("box_size_unit")
    private String boxSizeUnit;
    /**
    * 出库计费重：
    * 取值体积重量，出库重量最大值
    * 体积重量=出库尺寸/材积参数[取值渠道设置的材积设置]
    */
    @TableField("charged_weight")
    private BigDecimal chargedWeight;
    /**
    * 体积重
    */
    @TableField("volume_weight")
    private BigDecimal volumeWeight;
    /**
    * 单产品重量
    */
    @TableField("product_weight")
    private BigDecimal productWeight;
    /**
    * 分摊重量
    */
    @TableField("allocation_weight")
    private BigDecimal allocationWeight;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 物流商ID
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 物流商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 重量分摊方式
    */
    @TableField("allocation_type")
    private String allocationType;
    /**
    * 计费规则
    */
    @TableField("fee_rule")
    private String feeRule;
    /**
    * 店铺ID
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 目的国家编码
    */
    @TableField("to_country")
    private String toCountry;
    /**
    * 发货仓库ID
    */
    @TableField("from_warehouse_id")
    private String fromWarehouseId;
    /**
    * 核算期间id
    */
    @TableField("calculate_period_id")
    private String calculatePeriodId;
    /**
    * 核算月份
    */
    @TableField("calculate_month")
    private String calculateMonth;
    /**
     * 头程物流单ID
     */
    @TableField("logistics_bill_id")
    private String logisticsBillId;
    /**
     * 出库重量
     */
    @TableField("out_stock_weight")
    private BigDecimal outStockWeight;


    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String DELIVERY_DETAIL_ID = "delivery_detail_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String ALLOCATION_STATUS = "allocation_status";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_ID = "platform_sku_id";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String BOX_ID = "box_id";

    public static final String BOX_NO = "box_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String BOX_LENGTH = "box_length";

    public static final String BOX_WIDE = "box_wide";

    public static final String BOX_HIGH = "box_high";

    public static final String BOX_SIZE_UNIT = "box_size_unit";

    public static final String CHARGED_WEIGHT = "charged_weight";

    public static final String VOLUME_WEIGHT = "volume_weight";

    public static final String PRODUCT_WEIGHT = "product_weight";

    public static final String ALLOCATION_WEIGHT = "allocation_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String ALLOCATION_TYPE = "allocation_type";

    public static final String FEE_RULE = "fee_rule";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String TO_COUNTRY = "to_country";

    public static final String FROM_WAREHOUSE_ID = "from_warehouse_id";

    public static final String CALCULATE_PERIOD_ID = "calculate_period_id";

    public static final String CALCULATE_MONTH = "calculate_month";

    @Override
    public Serializable pkVal() {
        return null;
    }

}