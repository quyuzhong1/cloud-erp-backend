package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 产品管理批量修改字段枚举
 */
public enum ProductBatchFieldEnum implements EnumMessage  {

    PRODUCT_STATE("product_state","产品开发状态", "product_detail", "id"),
    SALE_STATE("sale_state","销售状态", "product_sale", "sku_id"),
    IS_MARKETABLE("is_marketable","是否可销售(0否，1是)", "product_sale", "sku_id"),
    PRODUCT_PROPERTY_ID("product_property_id","属性", "product_logistics", "sku_id"),
    DECLARE_CHINESE_NAME("declare_chinese_name","报关中文名", "product_logistics", "sku_id"),
    DECLARE_ENGLISH_NAME("declare_english_name","报关英文名", "product_logistics", "sku_id"),
    DECLARE_MODEL("declare_model","报关型号", "product_logistics", "sku_id"),
    DECLARE_PRICE("declare_price","报关申报价格", "product_logistics", "sku_id"),
    CUSTOMS_CODE("customs_code","中国海关编码", "product_logistics", "sku_id"),
    DECLARE_ELEMENT("declare_element","申报要素", "product_logistics", "sku_id"),
    ENGLISH_MATERIAL("english_material","英文材质", "product_logistics", "sku_id"),
    ENGLISH_USAGE("english_usage","英文用途", "product_logistics", "sku_id"),
    CHARGE_ID("charge_id","产品经理", "product_info", "id"),
    PURCHASE_USER_ID("purchase_user_id","采购员", "product_purchase", "sku_id"),
    SALE_METHOD("sale_method","销售方式", "product_info", "id"),
    GROSS_WEIGHT("gross_weight","毛重", "product_pack", "sku_id"),
    WAREHOUSE_LOCATION("warehouse_location","仓位", "product_detail", "id"),
    MAIN_SUPPLIER("main_supplier","一级供应商", "product_purchase", "sku_id"),
    SECOND_SUPPLIER("second_supplier","二级供应商", "product_purchase", "sku_id"),

    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 条件字段名称
     */
    private String keyName;


    ProductBatchFieldEnum(String code, String name, String tableName, String keyName) {
        this.code = code;
        this.name = name;
        this.tableName = tableName;
        this.keyName = keyName;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public String getKeyName() {
        return keyName;
    }

    public static String getName(String code) {
        for (ProductBatchFieldEnum fieldEnum : ProductBatchFieldEnum.values()) {
            if (code.equals(fieldEnum.getCode())) {
                return fieldEnum.getName();
            }
        }
        return "";
    }

    public static ProductBatchFieldEnum getEnumByCode(String code) {
        for (ProductBatchFieldEnum fieldEnum : ProductBatchFieldEnum.values()) {
            if (code.equals(fieldEnum.getCode())) {
                return fieldEnum;
            }
        }
        return null;
    }
}
