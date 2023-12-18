package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 单位枚举
 * @author Lambda
 * @Classname UnitEnum
 * @Description TODO
 * @Date 2023-11-13 15:01
 * @Created by yl
 */
public enum UnitEnum {
    ;
    /**
     * 重量单位枚举
     */
    @Getter
    @AllArgsConstructor
    public enum WeightUnitEnum implements EnumMessage{
        G("g","克"),
        KG("kg","千克")
        ;
        private final String code;
        private final String name;
    }

    /**
     * 时间单位枚举
     */
    @Getter
    @AllArgsConstructor
    public enum TimeUnitEnum implements EnumMessage{
        DAY("day","天")
        ;
        private final String code;
        private final String name;
    }
}
