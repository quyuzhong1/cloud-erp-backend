package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 客户表 账期设置 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-11-14 11:15:54
 */
public enum CustomerInfoPeriodSettingEnum implements EnumMessage {
	MONTH("month", "自然月"),
	PLATFORM("platform", "平台自定义账期"),
	SHOP("shop", "店铺自定义账期"),
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

    CustomerInfoPeriodSettingEnum(String code, String name) {
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
        for (CustomerInfoPeriodSettingEnum statusEnum : CustomerInfoPeriodSettingEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
