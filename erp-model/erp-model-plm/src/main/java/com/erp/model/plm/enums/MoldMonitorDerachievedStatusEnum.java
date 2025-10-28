package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 *  模具监控 达量状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-20 10:27:11
 */
public enum MoldMonitorDerachievedStatusEnum implements EnumMessage {
    UNDERACHIEVED("underachieved", "未达量"),
    DERACHIEVED("derachieved", "达量"),
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

    MoldMonitorDerachievedStatusEnum(String code, String name) {
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
        for (MoldMonitorDerachievedStatusEnum statusEnum : MoldMonitorDerachievedStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
