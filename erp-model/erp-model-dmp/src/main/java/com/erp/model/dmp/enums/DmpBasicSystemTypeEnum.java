package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 外部系统 系统类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpBasicSystemTypeEnum implements EnumMessage {
	WMS("wms", "仓储"),
	TMS("tms", "物流"),
	FINANCE("finance", "财务"),
    BI("bi", "BI"),
    OMS("oms", "销售平台"),
    THIRD_ERP("thirdErp", "第三方ERP"),
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

    DmpBasicSystemTypeEnum(String code, String name) {
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
        for (DmpBasicSystemTypeEnum statusEnum : DmpBasicSystemTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
