package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @description: 报关对账
 * @author Will
 * @date: 2024/3/26 15:28
 */
public enum TmsB2cDeclareReconciliationPayStatusEnum implements EnumMessage {

	PAYMENT("payment", "待付款"),
	PAID("paid", "已付款"),

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


    TmsB2cDeclareReconciliationPayStatusEnum(String code, String name) {
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
        for (TmsB2cDeclareReconciliationPayStatusEnum statusEnum : TmsB2cDeclareReconciliationPayStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static TmsB2cDeclareReconciliationPayStatusEnum getByCode(String code) {
        return Arrays.stream(TmsB2cDeclareReconciliationPayStatusEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}


