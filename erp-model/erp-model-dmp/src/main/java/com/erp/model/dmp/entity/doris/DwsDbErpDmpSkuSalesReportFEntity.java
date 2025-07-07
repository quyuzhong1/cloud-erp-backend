package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * SKU销量报告
 * </p>
 *
 * @author Jim
 * @since 2025-06-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain =true)
@TableName("dws_db_erp_dmp_sku_sales_report_f")
@NoArgsConstructor
public class DwsDbErpDmpSkuSalesReportFEntity extends BaseEntity<DwsDbErpDmpSkuSalesReportFEntity> {

    /**
    * 日均销量类型：dailyAvg3Days、dailyAvg7Days、dailyAvg30Days、dailyAvg60Days、dailyAvg90Days
    */
    @TableField("daily_sales_type")
    private String dailySalesType;
    /**
    * 日均销量
    */
    @TableField("daily_sales")
    private Integer dailySales;
    /**
    * 统计维度：deliveryTime=出库时间，paymentTime=付款时间  枚举：DwsDbErpDmpSkuSalesReportFDimensionEnum
    */
    @TableField("dimension")
    private String dimension;
    /**
    * SKU唯一ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * SKU名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 销售状态编码
    */
    @TableField("sale_state")
    private String saleState;
    /**
    * 销售状态名称
    */
    @TableField("sale_state_name")
    private String saleStateName;
    /**
    * SKU图片URL
    */
    @TableField("product_image_url")
    private String productImageUrl;
    /**
    * 一级分类ID
    */
    @TableField("first_category_id")
    private String firstCategoryId;
    /**
    * 一级分类名称
    */
    @TableField("first_category_name")
    private String firstCategoryName;
    /**
    * 二级分类ID
    */
    @TableField("second_category_id")
    private String secondCategoryId;
    /**
    * 二级分类名称
    */
    @TableField("second_category_name")
    private String secondCategoryName;
    /**
    * 销售平台编码
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 销售平台名称
    */
    @TableField("platform_name")
    private String platformName;
    /**
    * 客户ID
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 客户名称
    */
    @TableField("customer_name")
    private String customerName;
    /**
    * 国家ID
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 军区ID
    */
    @TableField("partition_id")
    private String partitionId;
    /**
    * 军区名称
    */
    @TableField("partition_name")
    private String partitionName;
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
    * 统计日期
    */
    @TableField("stat_date")
    private LocalDate statDate;
    /**
     * 应用分类ID
     */
    @TableField("application_category_id")
    private String applicationCategoryId;
    /**
     * 应用分类名称
     */
    @TableField("application_category_name")
    private String applicationCategoryName;


    public static final String DAILY_SALES_TYPE = "daily_sales_type";

    public static final String DAILY_SALES = "daily_sales";

    public static final String DIMENSION = "dimension";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String SALE_STATE = "sale_state";

    public static final String SALE_STATE_NAME = "sale_state_name";

    public static final String PRODUCT_IMAGE_URL = "product_image_url";

    public static final String FIRST_CATEGORY_ID = "first_category_id";

    public static final String FIRST_CATEGORY_NAME = "first_category_name";

    public static final String SECOND_CATEGORY_ID = "second_category_id";

    public static final String SECOND_CATEGORY_NAME = "second_category_name";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PARTITION_ID = "partition_id";

    public static final String PARTITION_NAME = "partition_name";

    public static final String SALES_LAST_3_DAYS = "sales_last_3_days";

    public static final String SALES_LAST_7_DAYS = "sales_last_7_days";

    public static final String SALES_LAST_30_DAYS = "sales_last_30_days";

    public static final String SALES_LAST_60_DAYS = "sales_last_60_days";

    public static final String SALES_LAST_90_DAYS = "sales_last_90_days";

    public static final String STAT_DATE = "stat_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
