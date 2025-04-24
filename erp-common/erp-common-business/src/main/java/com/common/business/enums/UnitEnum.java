package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 单位枚举
 * @author Lambda
 * @Classname UnitEnum
 * @Date 2023-11-13 15:01
 * @Created by yl
 */
public enum UnitEnum {
    ;
    /**
     * 重量单位枚举
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum WeightUnitEnum implements EnumMessage{
        G("g","g"),
        KG("kg","kg")
        ;
        public  String code;
        public  String name;

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    /**
     * 时间单位枚举
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum TimeUnitEnum implements EnumMessage{
        DAY("day","天")
        ;
        public  String code;
        public  String name;

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    /**
     * 重量单位枚举
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum SizeUnitEnum implements EnumMessage{
        CM("cm","厘米"),
        M("m","米")
        ;
        public  String code;
        public  String name;

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
