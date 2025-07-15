package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 操作方式枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-23 09:29:51
 */
public enum ThirdNoticeRecordOperationTypeEnum implements EnumMessage {
	UPDATE("U", "编辑"),
	INSERT("I", "新增"),
	DELETE("D", "删除"),
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

    ThirdNoticeRecordOperationTypeEnum(String code, String name) {
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
        for (ThirdNoticeRecordOperationTypeEnum statusEnum : ThirdNoticeRecordOperationTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
