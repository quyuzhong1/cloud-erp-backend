package com.erp.model.plm.enums;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 产品变更字段映射枚举
 * 映射：前端显示的变更字段名 -> 业务表/字段/数据类型
 */
public enum ProductChangeFieldEnum {
    // product_cost 分类
    EXPECTED_PROJECT_APPROVAL_COST("预计立项成本(¥)", "product_cost", "projectApprovalCost", BigDecimal.class),
    ACTUAL_MASS_PRODUCTION_COST("实际量产成本(¥)", "product_cost", "massCost", BigDecimal.class),
    EXPECTED_PROJECT_COST("预计项目成本(¥)", "product_cost", "projectCost", BigDecimal.class),
    TAX_RATE("税率(¥)", "product_cost", "taxRate", BigDecimal.class),
    TARGET_TAX_INCLUDED_COST("目标含税成本(¥)", "product_cost", "targetTaxCost", BigDecimal.class),
    STANDARD_RETAIL_PRICE("标准零售价(¥)", "product_cost", "retailPrice", BigDecimal.class),
    ACTUAL_GROSS_PROFIT_MARGIN("实际毛利率(美元)", "product_cost", "actualGpmUsd", BigDecimal.class),

    // product_detail 分类
    EXPECTED_ON_SHELF_TIME("预计上市时间", "product_detail", "planListingTime", LocalDate.class),

    // product_info 分类
    SALE_MODE("销售方式", "product_info", "saleMethod", String.class),
    PRODUCT_NAME_CN("产品款名（中文）", "product_info", "name", String.class),
    PRODUCT_SELLING_POINT("产品卖点", "product_info", "sellSpot", String.class),
    PRODUCT_USAGE("产品用途", "product_info", "usageDesc", String.class),
    MAIN_MATERIAL("主要材质", "product_info", "materials", String.class),
    PRODUCT_ATTRIBUTE("产品属性", "product_info", "propertyId", String.class),
    ENTRUSTED_DEVELOPMENT_COST("委托开发成本（¥）", "product_info", "entrustedDevelopCost", BigDecimal.class),
    SAMPLE_FEE("样品费用", "product_info", "sampleFee", BigDecimal.class),
    PRODUCT_NAME_EN("产品品名（英文）", "product_info", "nameEn", String.class),
    PRODUCT_CATEGORY("产品分类", "product_info", "categoryId", String.class),
    APPLICATION_CATEGORY("应用分类", "product_info", "applicationCategoryId", String.class),
    R_D_TEAM("研发团队（产线）", "product_info", "rdtTeamId", String.class),
    BRAND("品牌", "product_info", "brandId", String.class),
    PRODUCT_GRADE("产品等级", "product_info", "gradeId", String.class),
    SALE_CHANNEL("销售渠道", "product_info", "salesChannel", String.class),
    IS_CUSTOMIZED("是否客户定制", "product_info", "isCustomized", Integer.class),
    HAS_INFRINGEMENT_RISK("存在侵权风险", "product_info", "pirateRisk", Integer.class),

    // product_pack 分类
    PRODUCT_LENGTH("产品尺寸（长）（mm）", "product_pack", "productLength", BigDecimal.class),
    PRODUCT_WIDTH("产品尺寸（宽）（mm）", "product_pack", "productWidth", BigDecimal.class),
    PRODUCT_HEIGHT("产品尺寸（高）（mm)", "product_pack", "productHeight", BigDecimal.class),
    GROSS_WEIGHT("毛重（g）", "product_pack", "grossWeight", BigDecimal.class),
    NET_WEIGHT("净重（g）", "product_pack", "netWeight", BigDecimal.class),
    BOX_LENGTH("箱规（长）（mm）", "product_pack", "boxLength", BigDecimal.class),
    BOX_WIDTH("箱规（宽）（mm）", "product_pack", "boxWidth", BigDecimal.class),
    BOX_HEIGHT("箱规（高）（mm)", "product_pack", "boxHeight", BigDecimal.class),
    BOX_WEIGHT("单箱重量（kg）", "product_pack", "boxWeight", BigDecimal.class),
    BOX_QUANTITY("单箱数量", "product_pack", "boxQty", BigDecimal.class),

    // product_purchase 分类
    EAN_CODE("EAN码", "product_purchase", "ean", String.class),
    TRIAL_PRODUCTION_QUANTITY("试产数量", "product_purchase", "trialProductionQty", Long.class),
    FIRST_BATCH_MASS_PRODUCTION_QUANTITY("首批量产数量", "product_purchase", "firstMassQty", Long.class),
    PLANNED_FIRST_BATCH_ORDER_QUANTITY("计划首批下单量", "product_purchase", "planOrderQty", Long.class),
    EXPECTED_FIRST_BATCH_ARRIVAL_TIME("预计首批到货时间", "product_purchase", "planArrivalTime", LocalDate.class),
    MOQ("MOQ（最小起订量）", "product_purchase", "moq", Integer.class),
    DELIVERY_CYCLE("交货周期（天）", "product_purchase", "deliveryCycle", BigDecimal.class),
    FIRST_BATCH_ORDER_TIME("首批下单时间", "product_purchase", "placeOrderTime", LocalDate.class),
    ACTUAL_FIRST_BATCH_ARRIVAL_QUANTITY("实际首批到货量", "product_purchase", "actualArrivalQty", Long.class),
    ACTUAL_FIRST_BATCH_ARRIVAL_TIME("实际首批到货时间", "product_purchase", "actualArrivalTime", LocalDate.class),
    FIRST_BATCH_ARRIVAL_STATUS("首批到货状态", "product_purchase", "arrivalState", Integer.class),

    // product_ref_bu 分类
    BU_LINE("BU线", "product_ref_bu", "buId", String.class),

    // product_sale 分类
    ANNUAL_TARGET_SALES_VOLUME("年目标销量", "product_sale", "yearSaleQty", Long.class),
    ANNUAL_TARGET_SALES_AMOUNT("年目标销售额（¥）", "product_sale", "yearSaleAmount", BigDecimal.class),
    MONTHLY_TARGET_SALES_VOLUME("目标月销量", "product_sale", "monthSaleQty", Long.class),
    MONTHLY_TARGET_SALES_AMOUNT("目标月销售额（¥）", "product_sale", "monthSaleAmount", BigDecimal.class),
    COLLECTION_DEGREE_TARGET_SALES_VOLUME("首季度目标销量", "product_sale", "targetSalesQty", BigDecimal.class),
    SALE_COUNTRY("销售国家", "product_sale", "saleCountry", String.class),
    ON_SHELF_TIME("上市时间", "product_sale", "listingTime", LocalDate.class),
    OFF_SHELF_TIME("退市时间", "product_sale", "delistingTime", LocalDate.class),
    SALE_PLATFORM("销售平台", "product_sale", "salesPlatform", String.class),
    IS_IMAGE_COMPLETED("图片是否完成", "product_sale", "isFinishedImg", Integer.class),
    IS_VIDEO_COMPLETED("视频是否完成", "product_sale", "isFinishedVideo", Integer.class),
    ;

    // 前端下拉框显示的字段名
    private final String fieldLabel;

    private final String tableName;
    // 业务表的字段名（实体类属性名）
    private final String entityField;
    // 字段数据类型
    private final Class<?> dataType;

    ProductChangeFieldEnum(String fieldLabel, String tableName, String entityField, Class<?> dataType) {
        this.fieldLabel = fieldLabel;
        this.tableName = tableName;
        this.entityField = entityField;
        this.dataType = dataType;
    }

    // 根据前端显示的字段名获取枚举
    public static ProductChangeFieldEnum getByFieldLabel(String fieldLabel) {
        for (ProductChangeFieldEnum e : values()) {
            if (e.fieldLabel.equals(fieldLabel)) {
                return e;
            }
        }
        return null;
    }

    public static ProductChangeFieldEnum getByEntityField(String entityField) {
        for (ProductChangeFieldEnum e : values()) {
            if (e.entityField.equals(entityField)) {
                return e;
            }
        }
        return null;
    }

    // getter
    public String getFieldLabel() { return fieldLabel; }
    public String getTableName() { return tableName; }
    public String getEntityField() { return entityField; }
    public Class<?> getDataType() { return dataType; }
}