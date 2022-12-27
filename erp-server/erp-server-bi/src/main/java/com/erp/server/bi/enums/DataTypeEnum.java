package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/27 10:44
 */
public enum DataTypeEnum {

    DATAMARKET(0, "市场数据"),
    DATASCM(1, "供应链数据"),
    DATAMANAGE(2, "经营数据"),
    DATAFINANCE(3, "财务数据");

    private Integer code;
    private String name;

    DataTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (DataTypeEnum typeEnum : DataTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        DataTypeEnum[] enums = values();
        for (DataTypeEnum typeEnum : enums) {
            if (typeEnum.getName().equals(name)) {
                return typeEnum.getCode();
            }
        }
        return null;
    }
}
