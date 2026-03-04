package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * <p>
 *
 * </p>
 *
 * @author jack
 * @since 2025-09-16 10:01:37
 */
public enum FirstMileAllocationProcessEnum implements EnumMessage {
	PENDING("pending", "待执行"),
    PROCESSING("processing", "执行中"),
    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
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

    FirstMileAllocationProcessEnum(String code, String name) {
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
        for (FirstMileAllocationProcessEnum statusEnum : FirstMileAllocationProcessEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static FirstMileAllocationProcessEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }
}
