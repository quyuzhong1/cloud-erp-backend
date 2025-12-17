package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zdy
 * @version 1.0
 * @description: B2C销售订单多渠道类型枚举
 * @date 2025/8/22 12:27
 */
public enum SoB2cMultiChannelTypeEnum implements EnumMessage {

    AMAZON_APPROVING("amazonApproving",  "亚马逊多渠道发货审核中"),
    AMAZON_DELIVERY("amazonDelivery",  "亚马逊多渠道发货"),
    AMAZON_FAILED("amazonFailed",  "亚马逊多渠道发货创建失败"),
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


    SoB2cMultiChannelTypeEnum(String code, String name) {
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
        for (SoB2cMultiChannelTypeEnum soB2cCategoryTypeEnum : SoB2cMultiChannelTypeEnum.values()) {
            if (code.equals(soB2cCategoryTypeEnum.getCode())) {
                return soB2cCategoryTypeEnum.getName();
            }
        }
        return "";
    }
}
