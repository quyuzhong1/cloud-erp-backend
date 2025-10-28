package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 模具监控 类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-22 16:35:38
 */
public enum MoldMonitorTypeEnum implements EnumMessage {
    CFG_MOLD_RETURN_ALERT_RULE("cfgMoldReturnAlertrRule", "模具返还策略"),
    CFG_MOLD_ALERT_RULE("cfgMoldAlertrRule", "模具预警策略"),
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

    MoldMonitorTypeEnum(String code, String name) {
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
        for (MoldMonitorTypeEnum statusEnum : MoldMonitorTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
