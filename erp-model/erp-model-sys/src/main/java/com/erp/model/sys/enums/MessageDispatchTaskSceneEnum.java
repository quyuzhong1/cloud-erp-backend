package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum MessageDispatchTaskSceneEnum {
    SYS_NOTICE("SYS_NOTICE", "系统公告分发"),
    PDA_UPGRADE_PUSH("PDA_UPGRADE_PUSH", "PDA升级通知实时推送");

    public static final String LEGACY_PDA_NOTICE_CODE = "PDA_NOTICE";

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    MessageDispatchTaskSceneEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static boolean isSystemNoticeScene(String code) {
        return Objects.equals(SYS_NOTICE.code, code) || Objects.equals(LEGACY_PDA_NOTICE_CODE, code);
    }

    public static MessageDispatchTaskSceneEnum getByCode(String code) {
        if (isSystemNoticeScene(code)) {
            return SYS_NOTICE;
        }
        for (MessageDispatchTaskSceneEnum item : values()) {
            if (Objects.equals(item.code, code)) {
                return item;
            }
        }
        return null;
    }
}
