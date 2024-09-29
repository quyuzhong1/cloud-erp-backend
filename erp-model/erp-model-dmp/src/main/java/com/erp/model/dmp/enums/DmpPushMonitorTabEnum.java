package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 推送监控页面Tab状态
 */
public enum DmpPushMonitorTabEnum implements EnumMessage {
    INIT("init", "待推送"),
    PUSH_ING("pushIng", "推送中"),
    FINISH("finish", "推送成功"),
    ERROR("error", "推送失败"),
    NO_NEED_SYNC("noNeedSync", "无需同步"),
    BLACK("black", "黑名单"),
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

    DmpPushMonitorTabEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DmpPushMonitorTabEnum statusEnum : DmpPushMonitorTabEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

}
