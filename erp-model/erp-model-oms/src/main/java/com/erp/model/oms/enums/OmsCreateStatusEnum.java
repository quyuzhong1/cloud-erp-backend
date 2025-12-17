package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 创建状态 枚举
 * </p>
 *
 * @author lrp
 * @since 2025-08-28 18:51:58
 */
public enum OmsCreateStatusEnum implements EnumMessage {
	CREATING("creating", "创建中"),
	SUCCESS("success", "创建成功"),
	WAIT("wait", "待创建"),
	FAILED("failed", "创建失败"),
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

    OmsCreateStatusEnum(String code, String name) {
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
        for (OmsCreateStatusEnum statusEnum : OmsCreateStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
