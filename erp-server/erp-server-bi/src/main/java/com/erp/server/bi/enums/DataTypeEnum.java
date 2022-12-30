package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/27 10:44
 */
public enum DataTypeEnum {

    DATAMARKET(0, "市场数据","市场分析"),
    DATASCM(1, "供应链数据","供应链分析"),
    DATAMANAGE(2, "经营数据","经营分析"),
    DATAFINANCE(3, "财务数据","财务分析");

    private Integer code;
    private String name;
    private String desc;

    DataTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
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

    public static Integer getCodeByDesc(String desc) {
        DataTypeEnum[] enums = values();
        for (DataTypeEnum typeEnum : enums) {
            if (typeEnum.getDesc().equals(desc)) {
                return typeEnum.getCode();
            }
        }
        return null;
    }
}
