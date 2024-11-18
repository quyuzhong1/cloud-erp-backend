package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.config.JsonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;


/**
 * <p>
 * 产品备案表
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "product_registration", autoResultMap = true)
public class ProductRegistrationEntity extends BaseEntity<ProductRegistrationEntity> {

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
    * 报关平台
    */
    @TableField("declare_platform")
    private String declarePlatform;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 备案状态 draft 暂存  registering 备案中   registered 已备案  freeze 冻结  
    */
    @TableField("status")
    private String status;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 产品英文名称
    */
    @TableField("product_name_en")
    private String productNameEn;
    /**
    * 报关单位
    */
    @TableField("declare_unit")
    private String declareUnit;
    /**
    * 商品id
    */
    @TableField("good_id")
    private String goodId;
    /**
    * 产品型号
    */
    @TableField("spu")
    private String spu;
    /**
    * 申报币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 申报价格
    */
    @TableField("declare_price")
    private BigDecimal declarePrice;
    /**
    * 毛重(g)
    */
    @TableField("gross_weight")
    private BigDecimal grossWeight;
    /**
    * 长
    */
    @TableField("length")
    private BigDecimal length;
    /**
    * 宽
    */
    @TableField("width")
    private BigDecimal width;
    /**
    * 高
    */
    @TableField("height")
    private BigDecimal height;
    /**
    * 是否带电
    */
    @TableField("is_battery")
    private Boolean isBattery;
    /**
    * 电池类型
    */
    @TableField("battery_type")
    private String batteryType;
    /**
    * 报关中文名
    */
    @TableField("declare_name_cn")
    private String declareNameCn;
    /**
    * 海关编码
    */
    @TableField("customs_code")
    private String customsCode;
    /**
    * 第一数量
    */
    @TableField("first_number")
    private BigDecimal firstNumber;
    /**
    * 第二数量
    */
    @TableField("second_number")
    private BigDecimal secondNumber;
    /**
    * 申报要素
    */
    @TableField("declare_element")
    private String declareElement;
    /**
    * 供应商代码
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 条码类型
    */
    @TableField("barcode_type")
    private String barcodeType;
    /**
    * 自定义条码
    */
    @TableField("custom_barcode")
    private String customBarcode;
    /**
    * 是否带发票
    */
    @TableField("is_invoice")
    private Boolean isInvoice;
    /**
    * 是否零件类
    */
    @TableField("is_parts")
    private Boolean isParts;
    /**
    * 电池二级分类
    */
    @TableField("battery_note")
    private String batteryNote;
    /**
    * 图片url
    */
    @TableField("url")
    private String url;
    /**
     * 备案不通过原因
     */
    @TableField("failure_reason")
    private String failureReason;
    /**
     * 图片url
     */
    @TableField("latest_time")
    private LocalDateTime latestTime;

    /**
     * 报关商id
     */
    @TableField("declare_supplier_id")
    private String declareSupplierId;

    /**
     * 报关商名称
     */
    @TableField("declare_supplier_name")
    private String declareSupplierName;

    /**
     * 报关申报价币种符号
     */
    @TableField(value = "declare_currency_symbol")
    private String declareCurrencySymbol;

    /**
     * 推送信息
     */
    @TableField(value = "push_info", typeHandler = JsonTypeHandler.class)
    private Map<String, Object> pushInfo;

    /**
     * 拉取信息
     */
    @TableField(value = "pull_info", typeHandler = JsonTypeHandler.class)
    private Map<String, Object> pullInfo;
    /**
     * 报关型号
     */
    @TableField(value = "declare_model")
    private String declareModel;

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DECLARE_PLATFORM = "declare_platform";

    public static final String FIELD_REMARK = "remark";

    public static final String FIELD_STATUS = "status";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PRODUCT_NAME_EN = "product_name_en";

    public static final String DECLARE_UNIT = "declare_unit";

    public static final String GOOD_ID = "good_id";

    public static final String FIELD_SPU = "spu";

    public static final String FIELD_CURRENCY = "currency";

    public static final String DECLARE_PRICE = "declare_price";

    public static final String GROSS_WEIGHT = "gross_weight";

    public static final String FIELD_LENGTH = "length";

    public static final String FIELD_WIDTH = "width";

    public static final String FIELD_HEIGHT = "height";

    public static final String IS_BATTERY = "is_battery";

    public static final String BATTERY_TYPE = "battery_type";

    public static final String DECLARE_NAME_CN = "declare_name_cn";

    public static final String CUSTOMS_CODE = "customs_code";

    public static final String FIRST_NUMBER = "first_number";

    public static final String SECOND_NUMBER = "second_number";

    public static final String DECLARE_ELEMENT = "declare_element";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String BARCODE_TYPE = "barcode_type";

    public static final String CUSTOM_BARCODE = "custom_barcode";

    public static final String IS_INVOICE = "is_invoice";

    public static final String IS_PARTS = "is_parts";

    public static final String BATTERY_NOTE = "battery_note";

    public static final String FIELD_URL = "url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}