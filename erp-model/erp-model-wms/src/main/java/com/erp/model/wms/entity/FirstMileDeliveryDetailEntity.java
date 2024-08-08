package com.erp.model.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 头程发货单明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_delivery_detail")
public class FirstMileDeliveryDetailEntity extends BaseEntity<FirstMileDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;

    /**
    * 平台产品id（ASIN）
    */
    @TableField("platform_spu_no")
    private String platformSpuNo;
    /**
    * 平台sku（msku）
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 库存sku
    */
    @TableField("stock_sku")
    private String stockSku;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 应发数量
    */
    @TableField("plan_qty")
    private Integer planQty;
    /**
    * 实发数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 是否组合品
    */
    @TableField("is_combination")
    private Boolean isCombination;
    /**
    * 单品净重
    */
    @TableField("net_weight")
    private BigDecimal netWeight;
    /**
    * 产品尺寸（长）
    */
    @TableField("product_size_length")
    private BigDecimal productSizeLength;
    /**
    * 产品尺寸（宽）
    */
    @TableField("product_size_width")
    private BigDecimal productSizeWidth;
    /**
    * 产品尺寸（高）
    */
    @TableField("product_size_height")
    private BigDecimal productSizeHeight;
    /**
     * 最新签收日期
     */
    @TableField("receive_date")
    private LocalDateTime receiveDate;

    /**
     * 来源详情id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 仓位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 产品id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 审核状态
     */
    @TableField(exist = false)
    private String approveStatus;

    /**
     * FBA货件编码
     */
    @TableField("fba_shipment_code")
    private String fbaShipmentCode;



    public static final String MAIN_ID = "main_id";

    public static final String ASIN = "asin";

    public static final String M_SKU = "m_sku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String STOCK_SKU = "stock_sku";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String PLAN_QTY = "plan_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String IS_COMBO = "is_combo";

    public static final String NET_WEIGHT = "net_weight";

    public static final String PRODUCT_SIZE_LENGTH = "product_size_length";

    public static final String PRODUCT_SIZE_WIDTH = "product_size_width";

    public static final String PRODUCT_SIZE_HEIGHT = "product_size_height";

    @Override
    public Serializable pkVal() {
        return null;
    }

}