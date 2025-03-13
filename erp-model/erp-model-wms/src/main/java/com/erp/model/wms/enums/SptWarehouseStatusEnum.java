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
 * 仓库状态 0:不可用;1:可用;2:停用
 */
@Getter
@AllArgsConstructor
public enum SptWarehouseStatusEnum implements EnumMessage {
    UNAVAILABLE("0", "不可用"),
    AVAILABLE("1", "可用"),
    DISABLED("2", "停用"),
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
            for (SptWarehouseStatusEnum item : SptWarehouseStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SptWarehouseStatusEnum item : SptWarehouseStatusEnum.values()) {
                if (Objects.equals(name, item.getName())) {
                    return item.getCode();
                }
            }
        }
        return CharSequenceUtil.EMPTY;
    }

    public static SptWarehouseStatusEnum getByCode(String code) {
        SptWarehouseStatusEnum[] eumnList = SptWarehouseStatusEnum.values();
        for (SptWarehouseStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
