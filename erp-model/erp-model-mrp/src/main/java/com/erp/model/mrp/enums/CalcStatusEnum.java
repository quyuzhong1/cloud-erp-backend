package com.erp.model.mrp.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CalcStatusEnum implements EnumMessage {

    DOING("doing", "进行中"),
    FINISH("finish", "已完成");

    private final String code;

    private final String name;
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static CalcStatusEnum getEnum(String code) {
        for (CalcStatusEnum typeEnum : CalcStatusEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CalcStatusEnum statusEnum : CalcStatusEnum.values()) {
            if (CharSequenceUtil.equals(code,statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
