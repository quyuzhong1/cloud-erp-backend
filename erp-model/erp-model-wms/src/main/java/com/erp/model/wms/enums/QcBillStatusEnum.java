package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 质检单状态枚举
 * @author Lambda
 * @Classname QcBillStatusEnum
 * @Description TODO
 * @Date 2023-04-17 10:32
 * @Created by yl
 */
public enum QcBillStatusEnum {

    DRAFT("draft","暂存"),
    wait_qc("waitQc","待质检"),
    EXEMPTION("exemption","免检"),
    finish_qc("finishQc","已质检"),
    CANCEL("cancel","取消");

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


    QcBillStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
