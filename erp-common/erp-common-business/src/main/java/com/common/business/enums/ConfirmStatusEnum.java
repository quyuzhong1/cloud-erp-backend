package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @version 1.0

 * @date 2024/8/20 17:28
 */
public enum ConfirmStatusEnum implements EnumMessage  {
    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRM("confirm", "已确认");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ConfirmStatusEnum(String code, String name) {
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
            for (ConfirmStatusEnum item : ConfirmStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ConfirmStatusEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

    public static List<String> getCodeList() {
        return Arrays.stream(ConfirmStatusEnum.values()).map(ConfirmStatusEnum::getCode).collect(Collectors.toList());
    }
}
