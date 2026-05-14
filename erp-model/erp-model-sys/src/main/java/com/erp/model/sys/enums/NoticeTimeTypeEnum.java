package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * @Author: wtr
 * @Date: 2026/4/10 15:41
 * @Param:
 * @Return:
 * @Description:
 **/
public enum NoticeTimeTypeEnum {
    NOW("now","立即通知"),
    TIMING("timing","定时通知"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    NoticeTimeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getName(String code) {
        for (NoticeTimeTypeEnum messageTypeEnum : NoticeTimeTypeEnum.values()) {
            if (code.equals(messageTypeEnum.getCode())) {
                return messageTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        for (NoticeTimeTypeEnum messageTypeEnum : NoticeTimeTypeEnum.values()) {
            if(Objects.equals(name, messageTypeEnum.name)) {
                return messageTypeEnum.code;
            }
        }
        return "";
    }
}
