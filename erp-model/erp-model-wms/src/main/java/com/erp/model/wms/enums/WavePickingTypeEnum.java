package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
/**
 * 分拣方式枚举
 * @author will
 * @date 2024/7/3 11:34
 */
public enum WavePickingTypeEnum implements EnumMessage {

    FIRST_PICK("firstPick", "先拣后分"),
    SAME_PICK("samePick", "边拣边分"),
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

    WavePickingTypeEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (WavePickingTypeEnum item : WavePickingTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static WavePickingTypeEnum getByCode(String code) {
        WavePickingTypeEnum[] eumnList = WavePickingTypeEnum.values();
        for (WavePickingTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
