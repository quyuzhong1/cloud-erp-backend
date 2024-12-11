package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname OrderTypeEnum
 * @Description TODO
 * @Date 2023-11-23 19:18
 * @Created by yl
 */
public enum OrderTypeEnum implements EnumMessage {
    B2B("B2B", "B2B订单"),
    B2C("B2C", "B2C订单"),
    FIRST_MILE("firstMile", "头程物流单"),
    SORETURN_INSTOCK("soReturnInstock", "退货入库单"),
    ;

    private String code;

    private String name;

    OrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
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
        for (OrderTypeEnum item : OrderTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
