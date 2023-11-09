package com.erp.model.tms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicTypeEnum {

    LOGISTICS_SUPPLIER("logisticsSupplierType",  "物流商类型"),


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
            if (type.equals(dictBasic.getType())) {
                return dictBasic.getDesc();
            }
        }
        return "";
    }
}
