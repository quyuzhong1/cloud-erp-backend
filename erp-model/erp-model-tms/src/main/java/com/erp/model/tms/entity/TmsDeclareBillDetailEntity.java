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
 * 报关单明细
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_declare_bill_detail")
public class TmsDeclareBillDetailEntity extends BaseEntity<TmsDeclareBillDetailEntity> {

    /**
    * 主记录Id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku no
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 单价
    */
    @TableField("price")
    private BigDecimal price;
    /**
    * 中国海关编码
    */
    @TableField("customs_code")
    private String customsCode;
    /**
    * 报关中文名
    */
    @TableField("declare_chinese_name")
    private String declareChineseName;
    /**
    * 申报要素
    */
    @TableField("declare_element")
    private String declareElement;
    /**
    * 报关单位
    */
    @TableField("declare_unit")
    private String declareUnit;
    /**
    * 报关币别
    */
    @TableField("declare_currency")
    private String declareCurrency;
    /**
    * 报关币别符号
    */
    @TableField("declare_currency_symbol")
    private String declareCurrencySymbol;
    /**
    * 原产国
    */
    @TableField("source_country")
    private String sourceCountry;
    /**
    * 境内货源地
    */
    @TableField("source_cargo")
    private String sourceCargo;
    /**
    * 征免
    */
    @TableField("exemption")
    private String exemption;

    /**
     * 目的国
     */
    @TableField("to_country")
    private String toCountry;

    /**
     * 来源国名称
     */
    @TableField("source_country_name")
    private String sourceCountryName;

    /**
     * 目的国名称
     */
    @TableField("to_country_name")
    private String toCountryName;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String FIELD_QTY = "qty";

    public static final String FIELD_PRICE = "price";

    public static final String CUSTOMS_CODE = "customs_code";

    public static final String DECLARE_CHINESE_NAME = "declare_chinese_name";

    public static final String DECLARE_ELEMENT = "declare_element";

    public static final String DECLARE_UNIT = "declare_unit";

    public static final String DECLARE_CURRENCY = "declare_currency";

    public static final String DECLARE_CURRENCY_SYMBOL = "declare_currency_symbol";

    public static final String SOURCE_COUNTRY = "source_country";

    public static final String SOURCE_CARGO = "source_cargo";

    public static final String FIELD_EXEMPTION = "exemption";

    @Override
    public Serializable pkVal() {
        return null;
    }

}