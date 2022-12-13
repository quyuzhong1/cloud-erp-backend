package com.erp.server.plm.enums;

import com.common.core.constant.EnumMessage;

//销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
public enum  SaleStateEnum implements EnumMessage {
    NOT_SALE(1, "未销售"),
    SALES(2, "销售中"),
    CLEARANCE(3, "清仓中"),
    LOWER_SHELF(4, "已下架");

    private Integer code;
    private String name;

    SaleStateEnum(Integer code, String name) {
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
        SaleStateEnum[] saleMethodEnums = values();
        for (SaleStateEnum saleStateEnum : saleMethodEnums) {
            if (saleStateEnum.getCode() == code) {
                return saleStateEnum.getName();
            }
        }
        return null;
    }

    public static SaleStateEnum getEnumByType(String code){
        SaleStateEnum[] saleMethodEnums = values();
        for (SaleStateEnum saleStateEnum : saleMethodEnums) {
            if (saleStateEnum.getCode().equals(code)) {
                return saleStateEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        SaleStateEnum[] saleMethodEnums = values();
        for (SaleStateEnum saleStateEnum : saleMethodEnums) {
            if (saleStateEnum.getName().equals(name)) {
                return saleStateEnum.getCode();
            }
        }
        return null;
    }
}
