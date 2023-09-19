package com.erp.model.plm.enums;

/**
 * @Classname LabelLevelEnum

 * @Date 2023-09-18 10:05
 * @Created  zdy
 */
public enum LabelLevelEnum {
    /**
     * 标签级别 private 私有，company 公司';
     */
    PRIVATE("private", "私有"),
    COMPANY("company", "公司");

    private String type;
    private String name;


    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    LabelLevelEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getName(String type) {
        for (LabelLevelEnum item : LabelLevelEnum.values()) {
            if (type.equals(item.getType())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getType(String name) {
        for (LabelLevelEnum item : LabelLevelEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getType();
            }
        }
        return "";
    }
}
