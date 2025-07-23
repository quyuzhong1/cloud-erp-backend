package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


/**
 * <p>
 * B2C销售订单明细表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_detail")
public class SoB2cDetailEntity extends BaseEntity<SoB2cDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
     private String mainId;
    /**
    * 图片URL
    */
    @TableField("image_url")
    private String imageUrl;
    /**
    * skuId
    */
    @TableField("sku_id")
     private String skuId;
    /**
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台sku编号
    */
    @TableField("platform_sku_no")
     private String platformSkuNo;
    /**
    * 平台 产品id
    */
    @TableField("platform_spu_no")
         private String platformSpuNo;
    /**
    * 库存sku编号
    */
    @TableField("warehouse_sku_no")
    private String warehouseSkuNo;
    /**
    * 数量
    */
    @TableField("qty")
     private Integer qty;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
     private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
     private String warehouseName;
    /**
     * 虚拟仓库Id
     */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 单价
    */
    @TableField("price")
    private BigDecimal price;
    /**
    * 金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 币别（原币）
    */
    @TableField("currency")
     private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 建议售价（本位币）
    */
    @TableField("advice_price")
    private BigDecimal advicePrice;
    /**
    * 含税成本（本位币）
    */
    @TableField("tax_cost")
     private BigDecimal taxCost;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 标签json
    */
    private String labelJson;
    /**
    * 对应金蝶详情id
    */
    @TableField("kingdee_detail_id")
     private String kingdeeDetailId;
    /**
    * 库存组织id
    */
    @TableField("warehouse_org_id")
    private String warehouseOrgId;
    /**
    * 库存组织名称
    */
    @TableField("warehouse_org_name")
    private String warehouseOrgName;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 是否匹配仓库规则
     */
    @TableField("is_match_warehouse_rule")
    private Boolean isMatchWarehouseRule;

    /**
     * 平台明细行号
     */
    @TableField("platform_line_number")
    private String platformLineNumber;

    /**
     * 来源平台 SoB2cSourcePlatformEnum枚举
     */
    @TableField("source_platform")
    private String sourcePlatform;

    /**
     * 第三方平台的包裹号
     */
    @TableField("platform_package_id")
    private String platformPackageId;


    /**
     * 拆分的明细id
     */
    @TableField("split_detail_id")
    private String splitDetailId;

    /**
     * 原始sku(记录变更sku前原skuId)
     */
    @TableField("init_sku_id")
    private String initSkuId;

    /**
     * 当前净重
     */
    @TableField(exist = false)
    private BigDecimal currentNetWeight;

    /**
     * 操作明细集合id
     */
    @TableField(exist = false)
    private String operateDetailId;

    /**
     * 是否已标记发货
     */
    @TableField("is_sign_shipped")
    private Boolean isSignShipped;
    /**
     * 成本来源
     */
    @TableField("cost_source")
    private String costSource;
    /**
     * 材料成本（本位币）
     */
    @TableField("product_cost")
    private BigDecimal productCost;
    /**
     * 头程运费（本位币）
     */
    @TableField("first_mile_shipping_cost")
    private BigDecimal firstMileShippingCost;
    /**
     * 清关税费（本位币）
     */
    @TableField("clearance_customs_tax")
    private BigDecimal clearanceCustomsTax;

    /**
     * 还原id
     */
    @TableField(exist = false)
    private String revertId;

    /**
     * 第三方明细ID/编号
     */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
     * 扩展的 值 当后续有需要扩展的类型的字段值存里面
     */
    @TableField("extend_data")
    private String extendData;

    /**
     * 是否赠品：true/false
     */
    @TableField("is_gift")
    private Boolean isGift;
    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 销售费用
     */
    @TableField("sale_fee")
    private BigDecimal saleFee;

    /**
     * 变体属性
     */
    @TableField("variant_property")
    private String variantProperty;

    /**
     * 当前只有速卖通保存/可能速卖通没返回
     * 平台SKU ID
     */
    @TableField("platform_sku_id")
    private String platformSkuId;

    /**
     * 产品名称
     */
    @TableField(exist = false)
    private String productName;

    public static final String MAIN_ID = "main_id";

    public static final String IMAGE_URL = "image_url";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SELLER_SKU_NO = "seller_sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String STOCK_SKU_NO = "stock_sku_no";

    public static final String QTY = "qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String PRICE = "price";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String ADVICE_PRICE = "advice_price";

    public static final String COST = "cost";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String LABEL_JSON = "label_json";

    public static final String KINGDEE_DETAIL_ID = "kingdee_detail_id";

    public static final String WAREHOUSE_ORG_ID = "warehouse_org_id";

    public static final String WAREHOUSE_ORG_NAME = "warehouse_org_name";

    @Override
    public String toString() {
        return "SoB2cDetailEntity{" +
                "imageUrl='" + imageUrl + '\'' +
                ", skuId='" + skuId + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", platformSkuNo='" + platformSkuNo + '\'' +
                ", platformSpuNo='" + platformSpuNo + '\'' +
                ", warehouseSkuNo='" + warehouseSkuNo + '\'' +
                ", qty=" + qty +
                ", warehouseId='" + warehouseId + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", advicePrice=" + advicePrice +
                ", taxCost=" + taxCost +
                ", labelJson='" + labelJson + '\'' +
                ", kingdeeDetailId='" + kingdeeDetailId + '\'' +
                ", warehouseOrgId='" + warehouseOrgId + '\'' +
                ", warehouseOrgName='" + warehouseOrgName + '\'' +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", platformSkuId='" + platformSkuId + '\'' +
                '}';
    }
}