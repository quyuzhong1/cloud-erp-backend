package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 质检单状态枚举
 *
 * @author Lambda
 * @Classname QcBillStatusEnum

 * @Date 2023-04-17 10:32
 * @Created by yl
 */
public enum PutawayStatusEnum implements EnumMessage {

    WAIT("wait", "待上架"),
    PART("part", "部分上架"),
    FINISH("finish", "已上架"),
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


    PutawayStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PutawayStatusEnum getByCode(String code) {

        PutawayStatusEnum[] eumnList = PutawayStatusEnum.values();
        for (PutawayStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

}
