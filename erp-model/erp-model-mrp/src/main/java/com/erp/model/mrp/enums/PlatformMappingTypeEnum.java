package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlatformMappingTypeEnum implements EnumMessage {
    AMAZON_PLATFORM("amazonPlatform", "AMAZON", CfgRulePlatformTypeEnum.AMAZON),
    OVERSEAS_PLATFORM("overseasPlatform", "海外", CfgRulePlatformTypeEnum.OVERSEAS),
    INTERNAL_PLATFORM("internalPlatform", "国内", CfgRulePlatformTypeEnum.INTERNAL),
    B2B_PLATFORM("b2bPlatform", "B2B", CfgRulePlatformTypeEnum.B2B),
    ;

    private final String code;
    private final String name;
    private final CfgRulePlatformTypeEnum platformType;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 平台名称
     */
    public static String getByPlatformType(String platformType) {
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType)) {
            return AMAZON_PLATFORM.getCode();
        }
        if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(platformType)) {
            return OVERSEAS_PLATFORM.getCode();
        }
        if (CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(platformType)) {
            return INTERNAL_PLATFORM.getCode();
        }
        if (CfgRulePlatformTypeEnum.B2B.getCode().equals(platformType)) {
            return B2B_PLATFORM.getCode();
        }
        return "";
    }

    public static PlatformMappingTypeEnum getEnum(String code) {
        for (PlatformMappingTypeEnum typeEnum : PlatformMappingTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new ServiceException("枚举不存在");
    }
}
