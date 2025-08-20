package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流类型枚举
 * @author Lambda
 * @Classname LogisticsAddressEnums
 * @Date 2023-11-03 10:54
 * @Created by yl
 */
public enum TrackPlatformTypeEnum implements EnumMessage {
    TRACK123("TRACK123", "track123"),
    TRACK17("TRACK17", "17track"),
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

    TrackPlatformTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            if (name.equals(typeEnums.getName())) {
                return typeEnums.getCode();
            }
        }
        return "";
    }
}
