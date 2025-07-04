package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: b2c销售订单单据状态
 * @date 2023/8/21 12:05
 */
public enum SoB2cBillStatusEnum implements EnumMessage {
    ENUM_WAIT_DISTRIBUTION("waitDistribution",  "待配货"),
    ENUM_IN_DISTRIBUTION("inDistribution",  "配货中"),
    ENUM_WAIT_SHIPPED("waitShipped",  "待发货"),
    ENUM_SHIPPED("shipped",  "已发货"),
    ENUM_PARTIAL_SHIPPED("partialShipped",  "部分发货"),
    ENUM_FROZEN("frozen",  "冻结中"),

    //目前只有海外仓出库会用来判断，数据库不会更新这个状态
    ENUM_EXCEPTION("exception",  "异常订单"),
    ENUM_DISUSE("disuse",  "废弃"),
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


    SoB2cBillStatusEnum(String code, String name) {
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
        for (SoB2cBillStatusEnum soB2cBillStatusEnum : SoB2cBillStatusEnum.values()) {
            if (code.equals(soB2cBillStatusEnum.getCode())) {
                return soB2cBillStatusEnum.getName();
            }
        }
        return "";
    }
}
