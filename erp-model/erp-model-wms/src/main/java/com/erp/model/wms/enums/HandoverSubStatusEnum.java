package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author zdy
 * @Classname HandoverStatusEnum
 * @Description 小包状态code
 * @Date 2024-02-19 18:37
 * @Created by yl
 */
public enum HandoverSubStatusEnum implements EnumMessage {
    INIT("init","初始化"),
    INBOUND_NORMAL("inbound_normal","入站正常"),
    INBOUND_ABNORMAL("inbound_abnormal","入站异常"),
    CANCELING("canceling","取消中"),
    CANCELED("canceled","已取消"),
    REVERSE_WAIT("reverse_wait","等待退单"),
    REVERSE_PACKAGED("reverse_packaged","退单已组包"),
    REVERSE_SUCCESS("reverse_success","退单成功"),
    CLOSING("closing","关闭中"),
    CLOSED("closed","已关闭"),
    PICKUP_SUCCESS("pickup_success","揽收成功"),
    FULFILL_SUCCESS("fulfill_success","全部完成"),
    PRE_PICKUP_SUCCESS("pre_pickup_success","预揽收成功"),
    PRE_SIGN_UP_SUCCESS("pre_sign_up_success","预签收成功"),
    WAIT_PACKAGE("wait_package","等待组包"),
    PACKAGED("packaged","已组包"),
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

    HandoverSubStatusEnum(String code, String name) {
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

    public static String getByCode(String code) {

        HandoverSubStatusEnum[] enumList = HandoverSubStatusEnum.values();
        for (HandoverSubStatusEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
