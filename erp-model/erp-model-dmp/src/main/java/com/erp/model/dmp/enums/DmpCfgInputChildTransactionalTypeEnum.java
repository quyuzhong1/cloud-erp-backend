package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 推送任务记录 推送状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpCfgInputChildTransactionalTypeEnum implements EnumMessage {
	GLOBAL("global", "全局事务"),
	SINGLE("single", "单独事务"),
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

    DmpCfgInputChildTransactionalTypeEnum(String code, String name) {
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
        for (DmpCfgInputChildTransactionalTypeEnum statusEnum : DmpCfgInputChildTransactionalTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
