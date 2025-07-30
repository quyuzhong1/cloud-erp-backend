package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 合同模板主表 业务类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-07-24 09:36:52
 */
public enum TemplateManagementBizTypeEnum implements EnumMessage {
    PURCHASEFRAMEWORK("purchaseFramework", "采购框架合同"),
	SOCONTRACT("soContract", "销售订单合同"),
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

    TemplateManagementBizTypeEnum(String code, String name) {
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
        for (TemplateManagementBizTypeEnum statusEnum : TemplateManagementBizTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
