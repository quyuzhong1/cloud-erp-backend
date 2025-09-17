package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 中转仓平台枚举类
 *
 * @Author Cloud
 * @Date 2023/4/6 14:49
 **/
@Getter
public enum OmsPlatformEnum {
    OMS_GOOD_CANG("goodcang","谷仓海外仓"),
    OMS_IML("iml","艾姆勒海外仓"),
    OMS_ANTU("antu","安兔"),
    OMS_SPT("spt","速派通"),
    OMS_ECCANG("eccang","易仓"),
    JIFENG("jifeng","极风"),
    CAI_NIAO("cainiao","菜鸟仓"),
    WEI_SHI("weishi","纬狮"),
    DA_MAI("damai","大卖仓"),
    OMS_DHT("dht","订货通"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    OmsPlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static OmsPlatformEnum getByCode(String code) {
        OmsPlatformEnum[] values = values();
        for (OmsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
    public static String getName(String code) {
        OmsPlatformEnum[] values = values();
        for (OmsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }
    public static boolean isThirdWarehouse(String code) {
        OmsPlatformEnum[] values = values();
        for (OmsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> allPlatform() {
        return Arrays.stream(OmsPlatformEnum.values()).map(OmsPlatformEnum::getCode).collect(Collectors.toList());
    }
}
