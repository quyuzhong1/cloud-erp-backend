package com.erp.model.mrp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgNoticeObjectTypeEnum implements EnumMessage {

    NOTICE_USER("noticeUser","通知人员"),
    NOTICE_GROUP("noticeShopCharge","通知店铺负责人"),
    NOTICE_DAY("noticeDay","按天"),
    NOTICE_WEEK("noticeWeek","按周"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;


    public static String getName(String code) {
        for (CfgNoticeObjectTypeEnum settingEnum : CfgNoticeObjectTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgNoticeObjectTypeEnum getEnum(String code) {
        for (CfgNoticeObjectTypeEnum settingEnum : CfgNoticeObjectTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
