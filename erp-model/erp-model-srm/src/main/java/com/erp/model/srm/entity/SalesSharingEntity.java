package com.erp.model.srm.entity;

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
 * 销量共享表
 * </p>
 *
 * @author jack
 * @since 2025-06-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sales_sharing")
public class SalesSharingEntity extends BaseEntity<SalesSharingEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商编码
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 产品图片
    */
    @TableField("product_image")
    private String productImage;
    /**
    * 产品图片url
    */
    @TableField("product_image_url")
    private String productImageUrl;
    /**
    * sku_id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
    */
    @TableField("sale_state")
    private Integer saleState;
    /**
    * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
    */
    @TableField("sale_state_name")
    private String saleStateName;
    /**
    * 可销库存
    */
    @TableField("saleable_stock")
    private Integer saleableStock;
    /**
    * 原始日均销量
    */
    @TableField("daily_sales")
    private Integer dailySales;
    /**
    * 近3日销量
    */
    @TableField("sales_last_3_days")
    private Integer salesLast3Days;
    /**
    * 近7日销量
    */
    @TableField("sales_last_7_days")
    private Integer salesLast7Days;
    /**
    * 近30日销量
    */
    @TableField("sales_last_30_days")
    private Integer salesLast30Days;
    /**
    * 近60日销量
    */
    @TableField("sales_last_60_days")
    private Integer salesLast60Days;
    /**
    * 近90日销量
    */
    @TableField("sales_last_90_days")
    private Integer salesLast90Days;
    /**
    * 原始销量比例
    */
    @TableField("sales_ratio")
    private BigDecimal salesRatio;
    /**
    * 可销天数
    */
    @TableField("saleable_days")
    private Integer saleableDays;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String PRODUCT_IMAGE = "product_image";

    public static final String PRODUCT_IMAGE_URL = "product_image_url";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String SALE_STATE = "sale_state";

    public static final String SALEABLE_STOCK = "saleable_stock";

    public static final String DAILY_SALES = "daily_sales";

    public static final String SALES_LAST_3_DAYS = "sales_last_3_days";

    public static final String SALES_LAST_7_DAYS = "sales_last_7_days";

    public static final String SALES_LAST_30_DAYS = "sales_last_30_days";

    public static final String SALES_LAST_60_DAYS = "sales_last_60_days";

    public static final String SALES_LAST_90_DAYS = "sales_last_90_days";

    public static final String SALES_RATIO = "sales_ratio";

    public static final String SALEABLE_DAYS = "saleable_days";

    @Override
    public Serializable pkVal() {
        return null;
    }

}