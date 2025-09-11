package com.erp.model.oms.enums;

/**
 * @author Lambda
 * @Classname CfgSettingEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicTypeEnum {

    SUPPLIER_PAY_MODE("supplierPayMode",  "供应商结算方式"),
    SUPPLIER_CATEGORY("supplierCategory" , "供应商分类"),
    PLATFORM("platform" , "平台类型"),
    SALES_PLATFORM("salesPlatform" , "销售平台"),
    FULLY_MANAGED("fullyManaged" , "全托管平台类型"),
    SALES_PLATFORM_INTERNAL("internalSalesPlatform" , "国内"),
    SALES_PLATFORM_OVERSEAS("overseasSalesPlatform" , "海外"),
    SALES_PLATFORM_OTHER("otherSalesPlatform" , "其他"),
    SUPPLIER_ACCOUNT_PAYMENT("supplierAccountPayment","供应商账户付款方式"),
    RECEIVE_METHOD("receiveMethod",  "收款方式"),
    DHT_ACCOUNT_TYPE("dhtAccountType",  "订货通账户类型"),
    LOGISTICS_METHOD("logisticsMethod",  "b2c销售订单物流方式"),
    COMPARE("compare",  "订单规则的匹配"),
    FIELD("field","字段选项"),
    RULE_CONDITION("ruleCondition","审核规则"),
    SHOP_PLATFORM_COST("shopPlatformCost","店铺平台费率选项"),
    SHOP_VAT_COST("shopVATCost","店铺VAT费率选项"),
    SHOP_TRANSFER_COST("shopTransferCost","店铺转账费率选项"),
    DELIVERY_MODE("deliveryMode",  "交货方式"),
    TRADE_TERM("tradeTerm",  "贸易条款"),
    INVOICE_TAX_NFE_ORIGIN("invoiceTaxNfeOrigin",  "发票税务信息Nfe原产地"),

    CFG_SETTING("cfgSetting", "系统配置"),
    ORDER_SOURCE_TYPE("orderSourceType", "订单来源类型"),
    MINI_PROGRAM_SALES_PLATFORM_INTERNAL("miniProgramSalesPlatform" , "微信小程序国内销售平台"),

    CREDIT_PERIOD("creditPeriod",  "授信账期"),
    CREDIT_TYPE("creditType",  "授信类型"),

    // 数帝云
    SDY_SUB_PLATFORM("sdySubPlatform" , "数帝子平台映射"),
    SDY_PARTITION_LEVEL1_DEPT("sdyPartitionLevel1Dept" , "数帝云军区一级部门映射"),
    SDY_PLATFORM_LEVEL2_DEPT("sdyPlatformLevel2Dept" , "数帝云平台二级部门映射"),

    ;


    private String type;
    private String desc;


    DictBasicTypeEnum(String type, String desc) {

        this.type = type;
        this.desc = desc;
    }


    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static String getName(String type) {
        for (DictBasicTypeEnum dictBasic : DictBasicTypeEnum.values()) {
            if (dictBasic.getType().equals(type)) {
                return dictBasic.getDesc();
            }
        }
        return "";
    }
}
