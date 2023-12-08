package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 【海外仓入库单】
 * 入库单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OverseasInstockStatusEnum implements EnumMessage {
    TO_BE_SHIPPED("toBeShipped", "待发货"),
    TO_BE_SIGNED("toBeSigned", "待签收"),
    PARTIAL_SIGNED("partialSigned", "部分签收"),
    SIGNED("signed", "已签收"),
    AUTOMATIC_COMPLETION("automaticCompletion", "自动完结"),
    MANUAL_COMPLETION("manualCompletion", "手动完结"),
    CANCELED("canceled", "已取消"),
    ABNORMAL("abnormal", "异常"),
    ;

    @EnumValue
    @JsonValue
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (OverseasInstockStatusEnum item : OverseasInstockStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static OverseasInstockStatusEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }

    public static boolean canManualFinish(String code){
        //没有状态，默认可以完结
        if(StringUtils.isBlank(code)){
            return true;
        }
        return SIGNED.code.equals(code) || AUTOMATIC_COMPLETION.code.equals(code) || MANUAL_COMPLETION.code.equals(code) || CANCELED.code.equals(code);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(OverseasInstockStatusEnum.values()).map(OverseasInstockStatusEnum::getCode).collect(Collectors.toList());
    }
}
