package com.erp.model.plm.enums;

/**
 * @Description 销售方式枚举
 * @Author Luo_WG
 * @Date 2022/9/26 9:17
 * @param
 * @return
 **/
public enum SaleMethodEnum {
    GOODS(1, "商品"),
    GIFT(2, "赠品"),
    PACKAGING_MATERIALS(3, "包材");

    private Integer code;
    private String name;

    SaleMethodEnum(Integer code, String name) {
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
        SaleMethodEnum[] saleMethodEnums = values();
        for (SaleMethodEnum saleMethodEnum : saleMethodEnums) {
            if (saleMethodEnum.getCode() == code) {
                return saleMethodEnum.getName();
            }
        }
        return null;
    }

    public static SaleMethodEnum getEnumByType(String code){
        SaleMethodEnum[] saleMethodEnums = values();
        for (SaleMethodEnum saleMethodEnum : saleMethodEnums) {
            if (saleMethodEnum.getCode().equals(code)) {
                return saleMethodEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        SaleMethodEnum[] saleMethodEnums = values();
        for (SaleMethodEnum saleMethodEnum : saleMethodEnums) {
            if (saleMethodEnum.getName().equals(name)) {
                return saleMethodEnum.getCode();
            }
        }
        return null;
    }
}
