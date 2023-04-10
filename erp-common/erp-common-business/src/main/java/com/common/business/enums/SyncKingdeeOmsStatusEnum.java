package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 同步金蝶状态枚举类
 * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
 * @author Cloud
 */

public enum SyncKingdeeOmsStatusEnum {

    NOT_SYNC("0","无需同步"),

    BE_SYNC("1","待保存"),

    BE_SUBMIT("2","待提交"),

    BE_AUDIT("3","待审核"),

    SYNC_SUCCESS("4","同步成功"),

    SYNC_FAIL("9","同步失败")
    ;


    @EnumValue
    private String code;
    private String name;

    SyncKingdeeOmsStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode(){
        return this.code;
    }

    public String getName(){
        return this.name;
    }

    public static SyncKingdeeOmsStatusEnum getByCode(String code) {
        SyncKingdeeOmsStatusEnum[] values = values();
        for (SyncKingdeeOmsStatusEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
