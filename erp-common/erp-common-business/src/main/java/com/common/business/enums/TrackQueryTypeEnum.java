package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 轨迹查询单号类型枚举
 * @author Will
 * @version 1.0
 * @date 2024/4/23 15:49
 */
public enum TrackQueryTypeEnum implements EnumMessage {
    TRANSPORT_NO("transportNo","运单号"),
    TRACK_NO("trackNo","跟踪号"),
    ;

    TrackQueryTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
