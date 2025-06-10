package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * 第三方物流商编码枚举
 * @author zdy
 * @Classname LogisticTrackStatusEnum
 * @Date 2025-05-29 17:38
 * @Created by zdy
 */
public enum LogisticsThirdSupplierEnum implements EnumMessage {
    SF_EXPRESS("sfExpress","顺丰", TrackPlatformTypeEnum.TRACK123.getCode()),
    JT_EXPRESS("jtExpress","急兔",TrackPlatformTypeEnum.TRACK123.getCode()),
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
    private String type;


    LogisticsThirdSupplierEnum(String code, String name, String type){
        this.code = code;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getCode() {
        return this.code;
    }
    @Override
    public String getName() {
        return this.name;
    }
    public String getType() {
        return this.type;
    }


    /**
     * 通过code查询
     * LogisticsMethodEnum
     * 枚举
     */
    public static LogisticsThirdSupplierEnum getByName(String name, String type) {
        return Stream.of(LogisticsThirdSupplierEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(name) && e.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过code查询
     * 枚举
     */
    public static LogisticsThirdSupplierEnum getNameByCode(String code, String type) {
        return Stream.of(LogisticsThirdSupplierEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code) && e.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}
