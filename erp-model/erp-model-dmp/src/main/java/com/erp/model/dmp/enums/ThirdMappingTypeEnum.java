package com.erp.model.dmp.enums;

/**
 * @Author: wtr
 * @Date: 2026/3/5 17:29
 * @Param:
 * @Return:
 * @Description:
 **/
public enum  ThirdMappingTypeEnum {

    PROVINCE("province", "省"),
    CITY("city", "城市"),
    DISTRICT("district", "县"),
    LOGISTICS("logistics", "物流渠道"),
    PLATFORM("platform", "平台"),
    SHOP("shop", "店铺"),
    VIRTUAL_WAREHOUSE("virtualWarehouse", "虚拟仓"),
    WAREHOUSE("warehouse", "实体仓"),
            ;

    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    ThirdMappingTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ThirdMappingTypeEnum getByCode(String code) {
        ThirdMappingTypeEnum[] values = values();
        for (ThirdMappingTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
    public static String getNameByCode(String code) {
        ThirdMappingTypeEnum[] values = values();
        for (ThirdMappingTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.getName();
            }
        }
        return null;
    }

    public static ThirdMappingTypeEnum getByName(String name) {
        ThirdMappingTypeEnum[] values = values();
        for (ThirdMappingTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}


