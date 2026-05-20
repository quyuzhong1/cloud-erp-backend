package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum LogisticsStatusEnum implements EnumMessage {

    ORDERING("ordering", "下单中"),
    SUCCESS("success", "下单成功"),
    FAILED("failed", "下单失败"),
    CANCEL("cancel", "取消下单"),
    ;

    private final String code;

    private final String name;

    LogisticsStatusEnum(String code, String name) {
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
        for (LogisticsStatusEnum item : LogisticsStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
