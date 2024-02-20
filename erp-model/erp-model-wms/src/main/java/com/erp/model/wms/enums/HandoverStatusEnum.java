package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname HandoverStatusEnum
 * @Description TODO
 * @Date 2024-02-19 18:37
 * @Created by yl
 */
public enum HandoverStatusEnum implements EnumMessage {
    DRAFT("draft","草稿"),
    COMMITTED("committed","已提交"),
    AWAITING_TRACKING_NUMBER("awaitingTrackingNumber","等待分配运单号"),
    AWAITING_PACK("awaitingPack","等待商家关包确认"),
    AWAITING_DOMESTIC_PICKUP("awaitingDomesticPickup","等待快递揽收"),
    AWAITING_PICKUP("awaitingPickup","等待揽收"),
    PICKUP("pickup","已揽收"),
    DOMESTIC_PICKUP("domesticPickup","快递已揽收"),
    DOMESTIC_SIGN_IN("domesticSignIn","快递已签收"),
    PARTIAL_PICKED_UP("partialPickedUp","部分揽收"),
    PICKUP_FAILED("pickupFailed","揽收失败"),
    ARRIVED("arrived","已到达"),
    SIGNED_NORMAL("signedNormal","签收正常"),
    SIGNED_ABNORMAL("signedAbnormal","签收异常"),
    SIGNED_FAILED("signedFailed","签收失败"),
    CANCELING("canceling","取消中"),
    CANCELED("canceled","已取消"),
    CANCEL_FAILURE("cancelFailure","取消失败"),
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
