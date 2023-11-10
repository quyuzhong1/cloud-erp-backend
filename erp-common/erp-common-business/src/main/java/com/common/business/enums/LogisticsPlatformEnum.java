package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname LogisticsPlatformEnum1
 * @Description TODO
 * @Date 2023-11-09 16:14
 * @Created by yl
 */
public enum LogisticsPlatformEnum implements EnumMessage {
    SHOPEE("Shopee", "Shopee", "虾皮", "003"),
    //物流平台
    SDF("SDF", "递四方", "递四方(新)", ""),
    SF_EXPRESS("EXPRESS", "顺丰-丰桥", "顺丰国内物流", ""),
    UBI("UBI", "UBI", "UBI物流平台", ""),
    TRACK123("TRACK123", "track123", "track123物流平台", ""),
    YAN_WEN("YanWen", "燕文物流(新)", "燕文物流(新)", ""),
    WEI_SHI("WeiShi", "纬狮", "深圳前海纬狮物流网络科技有限公司", ""),
    YUN_TU("YunTu", "云途(新)", "云途(新)", ""),
    TONG_YOU("TongYou", "去发货(通邮)", "去发货(通邮)", "");


    @JsonValue
    @EnumValue
    private String code;

    private String name;

    private String desc;

    private String kingdeeCode;

    public String getDesc() {
        return desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }


    LogisticsPlatformEnum(String code, String name, String desc, String kingdeeCode){

        this.code = code;
        this.name = name;
        this.desc = desc;
        this.kingdeeCode = kingdeeCode;
    }


    public static LogisticsPlatformEnum getByCode(String code) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.code.equals(code) ) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByName(String name) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value.getName();
            }
        }
        return "";
    }

    public static LogisticsPlatformEnum getByName(String name) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
