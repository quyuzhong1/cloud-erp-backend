package com.erp.model.wms.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 速派通仓库类型 0标准 1中转 2虚拟
 */
@Getter
@AllArgsConstructor
public enum SptWarehouseTypeEnum implements EnumMessage {
    STANDARD("0", "标准"),
    TRANSIT("1", "中转"),
    VIRTUAL("2", "虚拟"),
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
            for (SptWarehouseTypeEnum item : SptWarehouseTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SptWarehouseTypeEnum item : SptWarehouseTypeEnum.values()) {
                if (Objects.equals(name, item.getName())) {
                    return item.getCode();
                }
            }
        }
        return CharSequenceUtil.EMPTY;
    }

    public static SptWarehouseTypeEnum getByCode(String code) {
        SptWarehouseTypeEnum[] eumnList = SptWarehouseTypeEnum.values();
        for (SptWarehouseTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
