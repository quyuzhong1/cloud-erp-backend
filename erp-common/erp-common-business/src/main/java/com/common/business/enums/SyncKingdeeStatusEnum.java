package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/9 8:59
 */
public enum SyncKingdeeStatusEnum {

    NO_NEED_SYNC("0", "无需同步"),
    TO_BE_SYNC("1", "待同步"),
    IN_SYNC("2", "同步中"),
    SUCCESS_SYNC("3", "同步成功"),
    FAILED_SYNC("4", "同步失败");

    private String code;

    private String name;


    SyncKingdeeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

}
