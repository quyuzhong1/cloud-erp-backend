package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 系统类型 枚举
 * </p>
 *
 * @author Jim
 * @since 2025-10-30 16:32:08
 */
public enum DmpDmpBasicSystemTypeEnum implements EnumMessage {
	WMS("wms", "仓储"),
	TMS("tms", "物流"),
	FINANCE("finance", "财务"),
	THIRDERP("thirdErp", "第三方ERP"),
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

    DmpDmpBasicSystemTypeEnum(String code, String name) {
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
        for (DmpDmpBasicSystemTypeEnum statusEnum : DmpDmpBasicSystemTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
