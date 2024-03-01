package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname HandoverStatusEnum
 * @Description 大包状态code
 * @Date 2024-02-19 18:37
 * @Created by yl
 */
public enum HandoverStatusEnum implements EnumMessage {
    DRAFT("draft","草稿"),
    COMMITTED("committed","已提交"),
    AWAITING_TRACKING_NUMBER("awaiting_tracking_number","等待分配运单号"),
    AWAITING_PACK("awaiting_pack","等待商家关包确认"),
    AWAITING_DOMESTIC_PICKUP("awaiting_domestic_pickup","等待快递揽收"),
    AWAITING_PICKUP("awaiting_pickup","等待揽收"),
    PICKUP("pickup","已揽收"),
    DOMESTIC_PICKUP("domestic_pickup","快递已揽收"),
    DOMESTIC_SIGN_IN("domestic_signIn","快递已签收"),
    PARTIAL_PICKED_UP("partial_picked_up","部分揽收"),
    PICKUP_FAILED("pickup_failed","揽收失败"),
    ARRIVED("arrived","已到达"),
    SIGNED_NORMAL("signed_normal","签收正常"),
    SIGNED_ABNORMAL("signed_abnormal","签收异常"),
    SIGNED_FAILED("signed_failed","签收失败"),
    CANCELING("canceling","取消中"),
    CANCELED("canceled","已取消"),
    CANCEL_FAILURE("cancel_failure","取消失败"),
    CLOSING("closing","关闭中"),
    CLOSED("closed","已关闭"),
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

    HandoverStatusEnum(String code, String name) {
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

        HandoverStatusEnum[] enumList = HandoverStatusEnum.values();
        for (HandoverStatusEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
