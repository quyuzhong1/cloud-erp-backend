package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * listing_info 平台状态
 */
public enum ListingInfoPlatformStatusEnum implements EnumMessage {
    ACTIVE("Active",	"在售"),
    INACTIVE("Inactive",	"停售"),
    INCOMPLETE("Incomplete",	"未完成"),
    UN_KNOW("unKnow",	"未知状态"),
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


    ListingInfoPlatformStatusEnum(String code, String name) {
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
        for (ListingInfoPlatformStatusEnum billTypeEnum : ListingInfoPlatformStatusEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (ListingInfoPlatformStatusEnum billTypeEnum : ListingInfoPlatformStatusEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
