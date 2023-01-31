package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: API字段取值方式枚举
 * @date 2023/1/11 14:58
 */
public enum KingdeeDocumentStatusEnum {

    CREATE_STATUS(1, "A", "自动创建"),
    PENDING_STATUS(2, "B", "待开发"),
    NORMAL_STATUS(3, "C", "正常"),
    CLEARANCE(4, "D", "清仓"),
    STOP_SELL(5, "E", "停止销售"),
    ;



    private Integer code;

    private String name;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    KingdeeDocumentStatusEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static KingdeeDocumentStatusEnum getByCode(Integer code) {
        KingdeeDocumentStatusEnum[] values = values();
        for (KingdeeDocumentStatusEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static KingdeeDocumentStatusEnum getByName(String name) {
        KingdeeDocumentStatusEnum[] values = values();
        for (KingdeeDocumentStatusEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
