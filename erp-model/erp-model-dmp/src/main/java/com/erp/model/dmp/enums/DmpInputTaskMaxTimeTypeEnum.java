package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 拉取任务 最大时间类型 枚举
 * </p>
 *
 */
public enum DmpInputTaskMaxTimeTypeEnum implements EnumMessage {

    // 当前时间=全量拉取
    NOW("now", "当前时间"),
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

    DmpInputTaskMaxTimeTypeEnum(String code, String name) {
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
        for (DmpInputTaskMaxTimeTypeEnum statusEnum : DmpInputTaskMaxTimeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
