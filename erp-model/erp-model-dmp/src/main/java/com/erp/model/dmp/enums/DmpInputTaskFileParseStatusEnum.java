package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 拉取任务文件存储 解析状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpInputTaskFileParseStatusEnum implements EnumMessage {
	WAIT("wait", "待解析"),
	FINISH("finish", "已解析"),
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

    DmpInputTaskFileParseStatusEnum(String code, String name) {
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
        for (DmpInputTaskFileParseStatusEnum statusEnum : DmpInputTaskFileParseStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
