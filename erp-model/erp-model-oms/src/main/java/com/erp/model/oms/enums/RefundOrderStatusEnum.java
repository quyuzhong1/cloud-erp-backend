package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 *  退货订单枚举
 * @author Lambda
 * @Classname CommenTypeStatusEnum
 * @Description TODO
 * @Date 2023-08-28 14:38
 * @Created by yl
 */
public enum RefundOrderStatusEnum implements EnumMessage {
    PROCESSING("processing","处理中"),
    CANCEL("cancel","已取消"),
    FINISH("finish","已退款"),



    ;

    RefundOrderStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 标识
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;



    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getName(String code) {
        for (RefundOrderStatusEnum statusTypeEnum : RefundOrderStatusEnum.values()) {
            if (code.equals(statusTypeEnum.getCode())) {
                return statusTypeEnum.getName();
            }
        }
        return "";
    }
}
