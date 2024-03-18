package com.erp.model.tms.enums;

/**
 * @description: 字典类型枚举
 * @author Will
 * @date: 2023/11/8 11:02
 */
public enum DictBasicEnum {

    SHIPPING_TEMPLATE_COST("shippingTemplateCost",  "运费模板费用"),
    DISCOUNT_RATE("discountRate",  "折扣费率下拉"),
    FUEL_SURCHARGE_RATE("fuelSurchargeRate",  "燃油附加费率下拉"),
    SIDE("side",  "边长下拉"),
    VOTE("vote",  "票下拉"),
    LOGISTIC_TRACK_STATUS_GROUP("logisticTrackStatusGroup",  "运输状态组"),
    LOGISTIC_TRACK_STATUS("logisticTrackStatus",  "运输状态"),
    LOGISTICS_SUPPLIER("logisticsSupplierType","物流商类型"),
    TRANSFER_LOGISTICS_AUTH_STATUS("transferLogisticsAuthStatus","中转报关服务商授权状态"),
    CFG_SETTING("cfgSetting", "系统配置"),
    WEEK("week", "周"),
    MONTH("month", "月"),
    DICT_COST_ATTRIBUTION("dictCostAttribution", "费用归属"),
    DICT_COST_CATEGORY("dictCostCategory", "费用分类"),
    ;


    private String type;
    private String desc;


    DictBasicEnum(String type, String desc) {

        this.type = type;
        this.desc = desc;
    }


    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
