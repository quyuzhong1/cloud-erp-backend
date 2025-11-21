package com.erp.model.tms.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;

/**
 * @description: 推送交接文件类型
 */
public enum LogisticsHandoverDocTypeEnum {

    HANDOVER_PACKAGE("handoverPackage",  "大包交接文件")
    ;


    private String code;
    private String name;


    LogisticsHandoverDocTypeEnum(String code, String name) {

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
        for (FmDeliveryLogisticsStatusEnum statusEnum : FmDeliveryLogisticsStatusEnum.values()) {
            if (CharSequenceUtil.equals(statusEnum.getCode(),code)) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
