package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;


/**
 * @Author: wtr
 * @Date: 2025/10/24 15:32
 * @Param:
 * @Return:
 * @Description:
 **/
public enum AssetPurchaseOrderReceiveEnum {

    WAIT_RECEIVE("waitReceive", "待验收"),
    PART_RECEIVE("partReceive", "部分验收"),
    ALL_RECEIVE("allReceive", "已验收"),
    CLOSE("close", "已关闭");



    @JsonValue
    @EnumValue
    private final String code;
    private final String name;

    AssetPurchaseOrderReceiveEnum(String code, String name) {
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
        for (AssetPurchaseOrderReceiveEnum statusEnum : AssetPurchaseOrderReceiveEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
