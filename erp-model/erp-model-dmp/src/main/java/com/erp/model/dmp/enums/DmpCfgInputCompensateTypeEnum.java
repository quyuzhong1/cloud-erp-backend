package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 外部系统接口明细补偿 拉取历史类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-27 11:05:38
 */
public enum DmpCfgInputCompensateTypeEnum implements EnumMessage {
	DAY("day", "一天之前"),
	WEEK("week", "一周之前"),
	MONTH("month", "一月之前"),
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

    DmpCfgInputCompensateTypeEnum(String code, String name) {
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
        for (DmpCfgInputCompensateTypeEnum statusEnum : DmpCfgInputCompensateTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
