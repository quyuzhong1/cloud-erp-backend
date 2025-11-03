package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 合同模板主表 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-07-24 09:36:52
 */
public enum TemplateManagementStatusEnum implements EnumMessage {
	FINISHED("finished", "已发布"),
	NOT("not", "未发布"),
	FAILED("failed", "发布失败"),
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

    TemplateManagementStatusEnum(String code, String name) {
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
        for (TemplateManagementStatusEnum statusEnum : TemplateManagementStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
