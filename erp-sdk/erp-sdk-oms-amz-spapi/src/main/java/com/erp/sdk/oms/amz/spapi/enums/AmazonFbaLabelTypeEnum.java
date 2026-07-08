package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 亚马逊 SP-API FBA 货件标签 LabelType
 * <p>
 * 对应 {@code getLabels} 接口 LabelType 参数，取值见 Fulfillment Inbound API v0 文档。
 */
@Getter
@AllArgsConstructor
public enum AmazonFbaLabelTypeEnum {

    BARCODE_2D("BARCODE_2D", "二维条码标签"),
    UNIQUE("UNIQUE", "唯一箱号标签"),
    PALLET("PALLET", "托盘标签"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static AmazonFbaLabelTypeEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AmazonFbaLabelTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
