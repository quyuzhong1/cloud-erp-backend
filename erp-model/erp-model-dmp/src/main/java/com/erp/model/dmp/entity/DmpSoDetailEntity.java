package com.erp.model.dmp.entity;

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
 * 中台销售订单详情表
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_detail")
public class DmpSoDetailEntity extends BaseEntity<DmpSoDetailEntity> {

    /**
    * 订单主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源详情id
    */
    @TableField("third_detail_id")
    private String thirdDetailId = "";
    /**
    * 平台原始详情id
    */
    @TableField("platform_detail_id")
    private String platformDetailId = "";
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId = "";
    /**
    * 产品编码
    */
    @TableField("sku_no")
    private String skuNo = "";
    /**
    * 平台sku
    */
    @TableField("platform_sku")
    private String platformSku = "";
    /**
    * 平台产品id
    */
    @TableField("platform_spu_no")
    private String platformSpuNo = "";
    /**
    * 产品多属性
    */
    @TableField("specifics")
    private String specifics = "";
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 单位
    */
    @TableField("product_unit")
    private String productUnit = "";
    /**
    * 产品名称
    */
    @TableField("sku_name")
    private String skuName = "";
    /**
    * 产品图片
    */
    @TableField("sku_url")
    private String skuUrl = "";
    /**
    * 是否赠品：true/false
    */
    @TableField("is_gift")
    private Boolean isGift = false;
    /**
    * 仓库编码
    */
    @TableField("warehouse_id")
    private String warehouseId = "";
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName = "";
    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation = "";
    /**
    * 订单商品备注
    */
    @TableField("item_remark")
    private String itemRemark = "";
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 商品原始售价(折扣前单价)
    */
    @TableField("sell_price_origin")
    private BigDecimal sellPriceOrigin;
    /**
    * 折扣金额
    */
    @TableField("discount_amount")
    private BigDecimal discountAmount;
    /**
    * 商品售价(折扣后单价)
    */
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
    * 折扣后订单总金额
    */
    @TableField("after_amount")
    private BigDecimal afterAmount;
    /**
    * 运费
    */
    @TableField("shipping_cost")
    private BigDecimal shippingCost;
    /**
     * 币种编码
     */
     @TableField("currency_code")
     private String currencyCode = "";
    /**
    * 拓展字段
    */
    @TableField("extend_data")
    private String extendData = "{}";
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
     * 平台包裹号
     */
    @TableField("platform_package_id")
    private String platformPackageId;
    /**
     * 优惠额（亚马逊）
     */
    @TableField("discount")
    private BigDecimal discount;
    /**
     * 商品状态：shipped 已发货、cancel 已取消、unShipped未发货
     * 枚举：SoB2cItemStatusEnum
     */
    @TableField("item_status")
    private String itemStatus;

    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String SPECIFICS = "specifics";

    public static final String QTY = "qty";

    public static final String PRODUCT_UNIT = "product_unit";

    public static final String SKU_NAME = "sku_name";

    public static final String SKU_URL = "sku_url";

    public static final String IS_GIFT = "is_gift";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String ITEM_REMARK = "item_remark";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SELL_PRICE_ORIGIN = "sell_price_origin";

    public static final String DISCOUNT_AMOUNT = "discount_amount";

    public static final String SELL_PRICE = "sell_price";

    public static final String AFTER_AMOUNT = "after_amount";

    public static final String SHIPPING_COST = "shipping_cost";

    public static final String EXTEND_DATA = "extend_data";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String PLATFORM_PACKAGE_ID = "platform_package_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}