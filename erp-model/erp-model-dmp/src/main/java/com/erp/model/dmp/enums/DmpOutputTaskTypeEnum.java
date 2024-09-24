package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 推送任务 推送类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-07-01 16:29:02
 */
public enum DmpOutputTaskTypeEnum implements EnumMessage {
	INPUT("input", "输入任务"),
	HOTFIX("hotfix", "快速任务"),
    NORMAL("normal", "正常任务"),
    HISTORY("history", "历史任务"),

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

    DmpOutputTaskTypeEnum(String code, String name) {
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
        for (DmpOutputTaskTypeEnum statusEnum : DmpOutputTaskTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
