package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 样品调整类型枚举
 * @author wuhaotian
 * @since 2025-11-14
 */
@Getter
@AllArgsConstructor
public enum SampleAdjustmentTypeEnum implements EnumMessage {
    INVENTORY_PROFIT("inventoryProfit", "盘盈"),
    INVENTORY_LOSS("inventoryLoss", "盘亏"),
    OTHER("other", "其他"),
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (SampleAdjustmentTypeEnum item : SampleAdjustmentTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SampleAdjustmentTypeEnum getByCode(String code) {
        SampleAdjustmentTypeEnum[] enumList = SampleAdjustmentTypeEnum.values();
        for (SampleAdjustmentTypeEnum item : enumList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
