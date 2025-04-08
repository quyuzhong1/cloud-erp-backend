package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 通知节点类型
 * @Auther will
 * @Date 2025/2/12 16:13
 */
public enum CfgVirtualNoticeObjectTypeEnum implements EnumMessage {

    NOTICE_USER("noticeUser","通知人员"),
    NOTICE_GROUP("noticeGroup","通知群"),
    NOTICE_DAY("noticeDay","按天"),
    NOTICE_WEEK("noticeWeek","按周"),
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


    CfgVirtualNoticeObjectTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (CfgVirtualNoticeObjectTypeEnum settingEnum : CfgVirtualNoticeObjectTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgVirtualNoticeObjectTypeEnum getEnum(String code) {
        for (CfgVirtualNoticeObjectTypeEnum settingEnum : CfgVirtualNoticeObjectTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
