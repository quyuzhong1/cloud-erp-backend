package com.erp.model.tms.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;

/**
 * @description: 仓库类型
 * @author Will
 * @date: 2024/5/23 15:27
 */
public enum LogisticsChannelWarehouseTypeEnum {

    ENUM_ALL("all",  "全部仓库"),
    ENUM_PART("part",  "指定仓库"),
    ;


    private String code;
    private String name;


    LogisticsChannelWarehouseTypeEnum(String code, String name) {

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
