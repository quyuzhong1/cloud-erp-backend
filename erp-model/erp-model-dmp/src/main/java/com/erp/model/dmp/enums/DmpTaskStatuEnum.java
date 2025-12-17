package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DMP任务状态 枚举
 *
 */
public enum DmpTaskStatuEnum implements EnumMessage {
    WAIT_FINISH("waitFinish", "待完成"),
    ERROR("error", "异常"),
    FINISH("finish", "完成"),
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

    DmpTaskStatuEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static List<String> getStatusList() {
        return Arrays.stream(DmpTaskStatuEnum.values()).map(DmpTaskStatuEnum::getCode).collect(Collectors.toList());
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
        for (DmpTaskStatuEnum statusEnum : DmpTaskStatuEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
