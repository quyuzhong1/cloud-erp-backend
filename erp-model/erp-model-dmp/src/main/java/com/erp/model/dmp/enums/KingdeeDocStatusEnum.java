package com.erp.model.dmp.enums;

public enum KingdeeDocStatusEnum {
    SAVED("Z","暂存"),
    CREATED("A","创建"),
    APPROVING("B","审核中"),
    APPROVED("C","已审核"),
    REAPPROVE("D","重新审核")
    ;


    private String code;
    private String name;

    KingdeeDocStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode(){
        return this.code;
    }

    public String getName(){
        return this.name;
    }

    public static KingdeeDocStatusEnum getByCode(String code) {
        KingdeeDocStatusEnum[] values = values();
        for (KingdeeDocStatusEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
