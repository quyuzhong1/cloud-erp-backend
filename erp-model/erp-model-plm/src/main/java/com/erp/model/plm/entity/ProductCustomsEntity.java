package com.erp.model.plm.entity;

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
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_customs")
public class ProductCustomsEntity extends BaseEntity<ProductCustomsEntity> {


    /**
    * sku
    */
    @TableField("sku_id")
    private String skuId;

    /**
    * sku编号
    */
    @TableField(exist = false)
    private String skuNo;

    /**
    * 图片
    */
    @TableField(exist = false)
    private String imagesUrl;

    /**
    * 国家
    */
    @TableField("country")
    private String country;

    /**
    * 国家
    */
    @TableField("country_name")
    private String countryName;

    /**
    * 海关编码
    */
    @TableField("customs_code")
    private String customsCode;

    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
    * 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
    */
    @TableField("type")
    private String type;

    /**
     * 目的国申报价
     */
    @TableField("to_declare_price")
    private BigDecimal toDeclarePrice;
    /**
     * 目的国申报币种
     */
    @TableField("to_currency")
    private String toCurrency;
    /**
     * 货币符号
     */
    @TableField("to_currency_symbol")
    private String toCurrencySymbol;

    public static final String SKU_ID = "sku_id";

    public static final String FIELD_COUNTRY = "country";

    public static final String CUSTOMS_CODE = "customs_code";

    public static final String TAX_RATE = "tax_rate";

    public static final String FIELD_TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}