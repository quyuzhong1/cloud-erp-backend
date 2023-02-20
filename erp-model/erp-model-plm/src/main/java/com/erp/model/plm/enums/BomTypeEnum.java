package com.erp.model.plm.enums;

/**
 * @Classname BomTypeEnum
 * @Description TODO
 * @Date 2023-01-29 18:05
 * @Created by yl
 */
public enum BomTypeEnum {

    SINGLE("single", "单品BOM"),
    COMBINATION("combination", "销售套装BOM"),
    SUBMIT_AUDIT("submitAudit", "提交审核"),
    CREATE("create", "提交");

    private String type;
    private String name;


    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    BomTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getName(String type) {
        for (BomTypeEnum item : BomTypeEnum.values()) {
            if (type.equals(item.getType())) {
                return item.getName();
            }
        }
        return "";
    }
}
