package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流类型枚举
 * @author Lambda
 * @Classname LogisticsAddressEnums
 * @Date 2023-11-03 10:54
 * @Created by yl
 */
public enum LogisticsSupplierTypeEnum implements EnumMessage {
    SELF_DELIVER("selfDeliver","自发货物流"),
    FIRST_CARRIER("firstCarrier","头程物流"),
    OVERSEAS_WAREHOUSE("overseasWarehouse","海外仓物流"),
    CUSTOM("custom","自定义物流")
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

    LogisticsSupplierTypeEnum(String code, String name){
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
        for (LogisticsSupplierTypeEnum typeEnums : LogisticsSupplierTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
