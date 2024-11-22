package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * <p>
 * 拉取任务 任务类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpInputTaskTaskTypeEnum implements EnumMessage {
	NORMAL("normal", "正常任务"),
	HISTORY("history", "历史任务"),
	COMPENSATE("compensate", "补偿任务"),
	HOTFIX("hotfix", "及时任务"),
	CHILD("child", "子类任务"),
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

    DmpInputTaskTaskTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DmpInputTaskTaskTypeEnum getByType(String taskType) {
        return Arrays.stream(DmpInputTaskTaskTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(taskType))
                .findFirst()
                .orElse(null);
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
        for (DmpInputTaskTaskTypeEnum statusEnum : DmpInputTaskTaskTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
