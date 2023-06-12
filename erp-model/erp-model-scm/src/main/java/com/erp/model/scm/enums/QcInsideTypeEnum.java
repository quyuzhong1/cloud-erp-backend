package com.erp.model.scm.enums;

/**
 * @ClassName QcInsideTypeEnum
 * @Author: zhangchunlin
 * @Date: 2023/6/12 15:32
 * @Description: 内外检类型
 */

public enum QcInsideTypeEnum {

    INSIDE_QC("inside_qc", "内检"),
    OUTSIDE_QC("outside_qc", "外检"),
    ;

    private String code;
    private String name;

    QcInsideTypeEnum(String code, String name) {
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

    public static String getNameByCode(String code) {
        QcInsideTypeEnum[] stateEnums = values();
        for (QcInsideTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }

}
