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

    private String code;
    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    LabelLevelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        for (LabelLevelEnum item : LabelLevelEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        for (LabelLevelEnum item : LabelLevelEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }
}
