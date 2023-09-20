package com.erp.model.plm.enums;

/**
 * @Classname LabelLevelEnum

 * @Date 2023-09-18 10:05
 * @Created  zdy
 */
public enum LabelColorEnum {
    /**
     * 标签级别 private 私有，company 公司';
     */
    GREY("#9c9ca0", "灰色");
    private String code;
    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    LabelColorEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        for (LabelColorEnum item : LabelColorEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getType(String name) {
        for (LabelColorEnum item : LabelColorEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }
}
