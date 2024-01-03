package com.erp.model.dmp.enums;

/**
 * @author CLOUD
 * @version 1.0

 * @date 2023/3/10 9:46
 */
public enum CleanStatusEnum {

    NONE(-1, "无需清洗"),
    UNCLEAN(0, "未清洗"),
    CLEANING(1, "清洗中"),
    CLEANED(2, "清洗完成");

    private Integer code;

    private String name;


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    CleanStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
