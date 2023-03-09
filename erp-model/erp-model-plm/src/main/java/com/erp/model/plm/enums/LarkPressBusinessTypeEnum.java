package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

import java.util.Arrays;

/**
 * 飞书催办消息业务类型
 * @author Cloud
 */
public enum LarkPressBusinessTypeEnum implements EnumMessage {

    /**
     * 产品任务催办
     */
    PRODUCT_TASK("product_task","产品任务"),
    ;

    public String code;

    private String name;


    LarkPressBusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static LarkPressBusinessTypeEnum getByCode(String code) {
        return Arrays.stream(LarkPressBusinessTypeEnum.values()).filter(x -> x.getCode().equals(code)).findFirst().orElse(null);
    }
}
