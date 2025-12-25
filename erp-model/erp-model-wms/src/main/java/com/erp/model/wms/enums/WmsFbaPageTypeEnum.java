package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * FBA标签类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-12-24 18:59:43
 */
public enum WmsFbaPageTypeEnum implements EnumMessage {
	PACKAGELABEL_PLAIN_PAPER("PackageLabel_Plain_Paper", "每张美国信纸1个标签"),
	PACKAGELABEL_LETTER_2("PackageLabel_Letter_2", "每张美国信纸2个标签"),
	PACKAGELABEL_LETTER_6("PackageLabel_Letter_6", "每张美国信纸6个标签"),
	PACKAGELABEL_A4_2("PackageLabel_A4_2", "每张A4纸上2个标签"),
	PACKAGELABEL_A4_4("PackageLabel_A4_4", "每张A4纸上4个标签"),
	PACKAGELABEL_THERMAL("PackageLabel_Thermal", "热敏纸（亚马逊合作承运人UPS）"),
	PACKAGELABEL_THERMAL_UNIFIED("PackageLabel_Thermal_Unified", "热敏纸（ATS发货）"),
	PACKAGELABEL_THERMAL_NO_CARRIER_ROTATION("PackageLabel_Thermal_No_Carrier_Rotation", "热敏纸（亚马逊合作承运人DHL）"),
	PACKAGELABEL_THERMAL_NONPCP("PackageLabel_Thermal_NonPCP", "热敏纸（非亚马逊合作物流）"),
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

    WmsFbaPageTypeEnum(String code, String name) {
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
        for (WmsFbaPageTypeEnum statusEnum : WmsFbaPageTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
