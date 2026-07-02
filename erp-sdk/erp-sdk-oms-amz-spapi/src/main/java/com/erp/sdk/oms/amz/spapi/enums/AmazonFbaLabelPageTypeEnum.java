package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 亚马逊 SP-API FBA 货件标签 PageType
 * <p>
 * 对应 {@code getLabels} 接口 PageType 参数，与 WMS {@code FbaPageTypeEnum} code 保持一致。
 */
@Getter
@AllArgsConstructor
public enum AmazonFbaLabelPageTypeEnum {

    PLAIN_PAPER("PackageLabel_Plain_Paper", "每张美国信纸1个标签"),
    LETTER_2("PackageLabel_Letter_2", "每张美国信纸2个标签"),
    LETTER_6("PackageLabel_Letter_6", "每张美国信纸6个标签"),
    A4_2("PackageLabel_A4_2", "每张A4纸上2个标签"),
    A4_4("PackageLabel_A4_4", "每张A4纸上4个标签"),
    THERMAL("PackageLabel_Thermal", "热敏纸(亚马逊合作承运人UPS)"),
    THERMAL_UNIFIED("PackageLabel_Thermal_Unified", "热敏纸(ATS发货)"),
    THERMAL_NO_CARRIER_ROTATION("PackageLabel_Thermal_No_Carrier_Rotation", "热敏纸(亚马逊合作承运人DHL)"),
    THERMAL_NON_PCP("PackageLabel_Thermal_NonPCP", "热敏纸(非亚马逊合作物流)"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static AmazonFbaLabelPageTypeEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AmazonFbaLabelPageTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
