package com.erp.model.scm.enums;

/**
 * @Author: wtr
 * @Date: 2025/10/21 11:51
 * @Param:
 * @Return:
 * @Description:
 **/
public enum AssetPurchaseOrderTypeEnum {

    ASSET_PURCHASE("assetPurchase", "资产采购订单");

    private String code;
    private String name;

    AssetPurchaseOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        AssetPurchaseOrderTypeEnum[] stateEnums = values();
        for (AssetPurchaseOrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
