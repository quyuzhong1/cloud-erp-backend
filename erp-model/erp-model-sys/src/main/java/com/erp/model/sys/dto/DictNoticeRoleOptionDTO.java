package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-04
*/
@Data
@NoArgsConstructor
public class DictNoticeRoleOptionDTO implements Serializable {


    /**
     * 下拉值
     */
    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据类型
         */
        private String businessType;

        /**
         * 字段（驼峰命名）
         */
        private String field;

        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 排序
         */
        private Integer index;


    }

    /**
     * 下拉值
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         *查询的值
         */
        private String select;

        /**
         *条件设置
         */
        private String condition;

        /**
         * map的key
         */
        private String mapKey;
        /**
         * 符号 默认等于
         *     GT("gt",">", "大于"),
         *     GE("ge",">=", "大于等于"),
         *     LT("lt","<", "小于"),
         *     LE("le","<=", "小于等于"),
         *     EQ("eq","=", "等于"),
         *     NE("ne","!=", "不等于"),
         */
        private String compareCode = "eq";


    }


}