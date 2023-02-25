package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

//首批到货状态：1.未到货 2.已到货 3.部分到货
public enum  PurchaseStateEnum implements EnumMessage {
    NON_ARRIVAL(1, "未到货"),
    ARRIVED(2, "已到货"),
    PARTIAL_ARRIVAL(3, "部分到货");

    private Integer code;
    private String name;

    PurchaseStateEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        PurchaseStateEnum[] purchaseStateEnums = values();
        for (PurchaseStateEnum purchaseStateEnum : purchaseStateEnums) {
            if (purchaseStateEnum.getCode() == code) {
                return purchaseStateEnum.getName();
            }
        }
        return "";
    }

    public static PurchaseStateEnum getEnumByType(String code){
        PurchaseStateEnum[] purchaseStateEnums = values();
        for (PurchaseStateEnum purchaseStateEnum : purchaseStateEnums) {
            if (purchaseStateEnum.getCode().equals(code)) {
                return purchaseStateEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        PurchaseStateEnum[] purchaseStateEnums = values();
        for (PurchaseStateEnum purchaseStateEnum : purchaseStateEnums) {
            if (purchaseStateEnum.getName().equals(name)) {
                return purchaseStateEnum.getCode();
            }
        }
        return null;
    }
}
