package com.common.business.enums;

import lombok.Getter;

@Getter
public enum AfterSalePackStatusEnum {

    WAIT_PACKING("waitPacking", "待装箱"),
    PENDING("pending", "待提审"),
    UNDER_REVIEW("underReview", "复审中"),
    REVIEW_REJECT("reviewReject", "复核驳回"),
    SEALED_BOX("sealedBox", "已封箱");

    private final String code;
    private final String name;

    AfterSalePackStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getByName(String code) {
        for (AfterSalePackStatusEnum item : AfterSalePackStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
