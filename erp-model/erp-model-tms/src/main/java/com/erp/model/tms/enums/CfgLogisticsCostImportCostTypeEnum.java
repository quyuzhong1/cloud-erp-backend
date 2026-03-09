package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 费用项配置 费用来源 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-20 17:53:25
 */
public enum CfgLogisticsCostImportCostTypeEnum implements EnumMessage {
	API("api", "API"),
	EXCEL("excel", "线下表格"),
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

    CfgLogisticsCostImportCostTypeEnum(String code, String name) {
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

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgLogisticsCostImportCostTypeEnum statusEnum : CfgLogisticsCostImportCostTypeEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportCostTypeEnum statusEnum : CfgLogisticsCostImportCostTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
