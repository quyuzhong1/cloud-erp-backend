package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: b2c销售订单明细来源平台枚举
 * @author Will
 * @date: 2023/12/28 12:27
 */
public enum SoB2cSourcePlatformEnum {

    ENUM_SELF_ADD("selfAdd",  "ERP新增"),
    ENUM_THIRD_PLATFORM("thirdPlatform",  "第三方平台新增"),
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


    SoB2cSourcePlatformEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cSourcePlatformEnum platformEnum : SoB2cSourcePlatformEnum.values()) {
            if (code.equals(platformEnum.getCode())) {
                return platformEnum.getName();
            }
        }
        return "";
    }

}
