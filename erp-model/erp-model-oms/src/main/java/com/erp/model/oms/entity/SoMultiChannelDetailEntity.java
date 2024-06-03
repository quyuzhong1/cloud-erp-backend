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
 * 多渠道订单明细表
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_multi_channel_detail")
public class SoMultiChannelDetailEntity extends BaseEntity<SoMultiChannelDetailEntity> {

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
    * 平台sku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 平台产品id
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
    @TableField("label_json")
    private String labelJson;
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
    * 平台明细行号
    */
    @TableField("platform_line_number")
    private String platformLineNumber;
    /**
    * 来源平台，字典soB2cSourcePlatform
    */
    @TableField("source_platform")
    private String sourcePlatform;


    public static final String MAIN_ID = "main_id";

    public static final String IMAGE_URL = "image_url";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String WAREHOUSE_SKU_NO = "warehouse_sku_no";

    public static final String QTY = "qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String PRICE = "price";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String ADVICE_PRICE = "advice_price";

    public static final String TAX_COST = "tax_cost";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String LABEL_JSON = "label_json";

    public static final String WAREHOUSE_ORG_ID = "warehouse_org_id";

    public static final String WAREHOUSE_ORG_NAME = "warehouse_org_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String PLATFORM_LINE_NUMBER = "platform_line_number";

    public static final String SOURCE_PLATFORM = "source_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}