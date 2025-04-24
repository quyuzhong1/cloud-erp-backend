package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 中台订单退货状态
 */
public enum  DmpOrderReturnStatusEnum implements EnumMessage {
    ORDER_RETURN("orderReturn", "已退货"),
    PARTIAL_RETURN("partialReturn", "部分退货"),
    NOT_RETURN("notReturn", "未退货"),
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

    DmpOrderReturnStatusEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DmpOrderReturnStatusEnum statusEnum : DmpOrderReturnStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }


    /**
     * 通过code查询
     */
    public static DmpOrderReturnStatusEnum getByCode(String code){
        return Arrays.stream(DmpOrderReturnStatusEnum.values())
                .filter(e-> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
