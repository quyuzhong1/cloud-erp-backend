package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * b2c销售订单数据类型枚举，现用于远程查询时使用
 * @author will
 * @date 2024/6/25 20:10
 */
public enum SoB2cDataTypeEnum implements EnumMessage {
    DETAIL("detail","明细"),
    LOGISTIC("logistic","物流"),
    RECEIVER("receiver","买家"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    SoB2cDataTypeEnum(String code, String name) {
        this.code=code;
        this.name=name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cDataTypeEnum statusEnum : SoB2cDataTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
