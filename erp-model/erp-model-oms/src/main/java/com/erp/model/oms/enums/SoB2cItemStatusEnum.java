package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;


/**
 * B2C订单商品状态
 */
public enum SoB2cItemStatusEnum implements EnumMessage {
    SHIPPED("shipped",  "已发货"),
    CANCEL("cancel",  "已取消"),
    UN_SHIPPED("unShipped",  "未发货"),
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


    SoB2cItemStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cItemStatusEnum soB2cItemStatusEnum : SoB2cItemStatusEnum.values()) {
            if (code.equals(soB2cItemStatusEnum.getCode())) {
                return soB2cItemStatusEnum.getName();
            }
        }
        return "";
    }
}
