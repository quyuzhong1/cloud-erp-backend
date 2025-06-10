package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompareDTO {

        /**
         * 左括号
         */
        private String leftBracket;

        /**
         * 条件的字段
         */
        private String field;

        /**
         * 比较符
         */
        private String compare;

        /**
         * 对应的值
         */
        private String value;

        /**
         * 右括号
         */
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and
         */
        private String logic;

        // 重写 equals 和 hashCode 保证可以在 Set 中正确去重比较
        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CompareDTO)) return false;
                CompareDTO that = (CompareDTO) o;
                return Objects.equals(leftBracket, that.leftBracket) &&
                        Objects.equals(field, that.field) &&
                        Objects.equals(compare, that.compare) &&
                        Objects.equals(value, that.value) &&
                        Objects.equals(rightBracket, that.rightBracket) &&
                        Objects.equals(logic, that.logic);
        }

        @Override
        public int hashCode() {
                return Objects.hash(leftBracket, field, compare, value, rightBracket, logic);
        }
    }