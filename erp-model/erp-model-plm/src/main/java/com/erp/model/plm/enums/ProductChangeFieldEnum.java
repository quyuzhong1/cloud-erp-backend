package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

/**
 * 产品变更字段映射枚举
 * 映射：前端显示的变更字段名 -> 业务表/字段/数据类型
 */
public enum ProductChangeFieldEnum implements EnumMessage {
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
    PRODUCT_DETAIL_NAME_CN("产品品名（中文）", "product_detail", "name", String.class),
    PRODUCT_DETAIL_NAME_EN("产品品名（英文）", "product_detail", "nameEn", String.class),

    // product_info 分类
    SALE_MODE("销售方式", "product_info", "saleMethod", String.class),
    PRODUCT_NAME_CN("产品款名（中文）", "product_info", "spuName", String.class),
    PRODUCT_SELLING_POINT("产品卖点", "product_info", "sellSpot", String.class),
    PRODUCT_USAGE("产品用途", "product_info", "usageDesc", String.class),
    MAIN_MATERIAL("主要材质", "product_info", "materials", String.class),
    PRODUCT_ATTRIBUTE("产品属性", "product_info", "propertyId", String.class),
    ENTRUSTED_DEVELOPMENT_COST("委托开发成本（¥）", "product_info", "entrustedDevelopCost", BigDecimal.class),
    SAMPLE_FEE("样品费用", "product_info", "sampleFee", BigDecimal.class),
    PRODUCT_NAME_EN("产品款名（英文）", "product_info", "spuNameEn", String.class),
    PRODUCT_CATEGORY("产品分类", "product_info", "categoryId", String.class),
    APPLICATION_CATEGORY("应用分类", "product_info", "applicationCategoryId", String.class),
    R_D_TEAM("研发团队（产线）", "product_info", "rdtTeamId", String.class),
    BRAND("品牌", "product_info", "brandId", String.class),
    PRODUCT_GRADE("产品等级", "product_info", "gradeId", String.class),
    SALE_CHANNEL("销售渠道", "product_info", "salesChannel", String.class),
    IS_CUSTOMIZED("是否客户定制", "product_info", "isCustomized", Integer.class, s -> "是".equals(s) ? 1 : ("否".equals(s) ? 0 : Integer.parseInt(s))),
    HAS_INFRINGEMENT_RISK("存在侵权风险", "product_info", "pirateRisk", Integer.class,s -> "有风险".equals(s) ? 1 : ("无风险".equals(s) ? 2 : Integer.parseInt(s))),

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
    FIRST_BATCH_ARRIVAL_STATUS("首批到货状态", "product_purchase", "arrivalState", Integer.class,s -> "未到货".equals(s) ? 1 : ("已到货".equals(s) ? 2 :("部分到货".equals(s)?3:Integer.parseInt(s)) )),

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
    IS_IMAGE_COMPLETED("图片是否完成", "product_sale", "isFinishedImg", Integer.class, s -> "是".equals(s) ? 1 : ("否".equals(s) ? 2 : Integer.parseInt(s))),
    IS_VIDEO_COMPLETED("视频是否完成", "product_sale", "isFinishedVideo", Integer.class, s -> "是".equals(s) ? 1 : ("否".equals(s) ? 2 : Integer.parseInt(s))),
    ;

    // 前端下拉框显示的字段名
    private final String name;

    private final String tableName;
    // 业务表的字段名（实体类属性名）
    private final String code;
    // 字段数据类型
    private final Class<?> dataType;

    private final Function<String, Object> converter;  // 第五个参数：自定义转换器

    // 四个参数的构造函数，调用五个参数的构造函数并传入 null 转换器（表示使用默认转换逻辑）
    ProductChangeFieldEnum(String name, String tableName, String code, Class<?> dataType) {
        this(name, tableName, code, dataType, null);
    }

    // 五个参数的构造函数，允许传入自定义转换器
    ProductChangeFieldEnum(String name, String tableName, String code, Class<?> dataType,
                           Function<String, Object> converter) {
        this.name = name;
        this.tableName = tableName;
        this.code = code;
        this.dataType = dataType;
        this.converter = converter;
    }
    // 根据前端显示的字段名获取枚举
    public static ProductChangeFieldEnum getByFieldLabel(String fieldLabel) {
        for (ProductChangeFieldEnum e : values()) {
            if (e.name.equals(fieldLabel)) {
                return e;
            }
        }
        return null;
    }

    public static ProductChangeFieldEnum getByEntityField(String entityField) {
        for (ProductChangeFieldEnum e : values()) {
            if (e.code.equals(entityField)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 使用枚举的转换器（如果存在）将字符串转换为目标类型的对象；
     * 如果转换器为 null，则调用默认的转换方法 {@link #convertDefault(String)}。
     */
    public Object convert(String value) {
        if (converter != null) {
            return converter.apply(value);
        }
        return convertDefault(value);
    }

    /**
     * 默认转换逻辑：根据 dataType 将字符串转换为对应类型。
     * 对于 Integer 类型，如果字符串是“是”/“否”，同样转换为 1/0，以保持统一。
     */
    private Object convertDefault(String value) {
        if (value == null) {
            return null;
        }
        if (dataType == String.class) {
            return value;
        } else if (dataType == Integer.class) {
            // 支持“是”/“否”转换，同时保留数字字符串的解析
            if ("是".equals(value)) {
                return 1;
            } else if ("否".equals(value)) {
                return 0;
            } else {
                return Integer.parseInt(value);
            }
        } else if (dataType == Long.class) {
            return Long.parseLong(value);
        } else if (dataType == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (dataType == LocalDate.class) {
            return LocalDate.parse(value); // 假设格式为 yyyy-MM-dd
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
    }

    // getter
    public String getName() { return name; }
    public String getTableName() { return tableName; }
    public String getCode() { return code; }
    public Class<?> getDataType() { return dataType; }
}