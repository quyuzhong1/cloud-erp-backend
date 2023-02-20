package com.erp.model.plm.enums;

public enum  BasicDictTypeEnum {
    /**
     * 产品属性
     */
    PRODUCT_PROPERTY("productProperty", "产品属性"),
    /**
     * 产品等级
     */
    PRODUCT_GRADE("productGrade", "产品等级"),
    /**
     * 产品品牌
     */
    PRODUCT_BRAND("productBrand", "产品品牌"),
    /**
     * 报关属性
     */
    DECLARE_PROPERTY("declareProperty", "报关属性"),
    /**
     * 国家
     */
    COUNTRY("country", "国家");

    private String code;
    private String name;

    BasicDictTypeEnum(String code, String name) {
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

    public static String getNameByCode(Integer code) {
        BasicDictTypeEnum[] basicDictTypeEnums = values();
        for (BasicDictTypeEnum basicDictTypeEnum : basicDictTypeEnums) {
            if (basicDictTypeEnum.getCode().equals(code)) {
                return basicDictTypeEnum.getName();
            }
        }
        return null;
    }

    public static BasicDictTypeEnum getEnumByType(String code){
        BasicDictTypeEnum[] basicDictTypeEnums = values();
        for (BasicDictTypeEnum basicDictTypeEnum : basicDictTypeEnums) {
            if (basicDictTypeEnum.getCode().equals(code)) {
                return basicDictTypeEnum;
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        BasicDictTypeEnum[] basicDictTypeEnums = values();
        for (BasicDictTypeEnum basicDictTypeEnum : basicDictTypeEnums) {
            if (basicDictTypeEnum.getName().equals(name)) {
                return basicDictTypeEnum.getCode();
            }
        }
        return null;
    }

    public static BasicDictTypeEnum[] ListBasicDictType() {
        return values();
    }
}
