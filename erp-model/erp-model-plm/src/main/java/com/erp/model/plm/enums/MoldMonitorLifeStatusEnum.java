package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具监控 寿命状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-22 16:35:38
 */
public enum MoldMonitorLifeStatusEnum implements EnumMessage {
	HEALTHY("healthy", "健康"),
	ALERT("alert", "预警"),
	EXHAUSTED("exhausted", "耗尽"),
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

    MoldMonitorLifeStatusEnum(String code, String name) {
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
        for (MoldMonitorLifeStatusEnum statusEnum : MoldMonitorLifeStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(MoldMonitorLifeStatusEnum.values()).map(MoldMonitorLifeStatusEnum::getCode).collect(Collectors.toList());
    }
}
