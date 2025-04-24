package com.erp.model.mrp.enums;

import cn.hutool.core.util.StrUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum SuggestStatusEnum implements EnumMessage {
    DRAFT("draft", "草稿"),
    WAIT_CONFIRM("waitConfirm", "待确认"),
    FINISH("finish", "已完成"),
    ;

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

    public static SuggestStatusEnum getEnum(String code) {
        for (SuggestStatusEnum typeEnum : SuggestStatusEnum.values()) {
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
        for (SuggestStatusEnum statusEnum : SuggestStatusEnum.values()) {
            if (StrUtil.equals(code,statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
