package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 复检抽检结果枚举
 * @author zhangchunlin
 * @Classname QcCheckResultEnum

 * @Date 2023-07-24 9:59
 * @Created by yl
 */
public enum QcReCheckResultEnum {

    OK ("OK", "OK"),
    NG("NG", "NG");

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

    QcReCheckResultEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getByCode(String code) {
        QcReCheckResultEnum qcResultEnum  = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcResultEnum != null) {
            return qcResultEnum.getName();
        }
        return "";
    }
}
