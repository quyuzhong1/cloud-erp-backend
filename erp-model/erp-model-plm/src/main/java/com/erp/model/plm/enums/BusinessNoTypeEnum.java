package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 系统编码
 * @date 2022/11/22 10:26
 */
public enum BusinessNoTypeEnum {

    SKU_NO(1, "sku_no"),
    SPU_NO(2, "spu_no"),
    Bom_NO(3, "bom_no");

    private Integer code;
    private String name;

    BusinessNoTypeEnum(Integer code, String name) {
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
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().equals(code)) {
                return businessNoTypeEnum.getName();
            }
        }
        return null;
    }

    public static BusinessNoTypeEnum getEnumByType(String code){
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().equals(code)) {
                return businessNoTypeEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getName().equals(name)) {
                return businessNoTypeEnum.getCode();
            }
        }
        return null;
    }
}
