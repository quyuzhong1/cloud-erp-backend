package com.erp.model.oms.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 规则条件表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleConditionDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        private String id;
        /**
         * 规则id
         */
        private String ruleId;

        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 比较浮名称
         */
        private String compareName;

        /**
         * 后面的逻辑名
         */
        private String logicName;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 值对应的名称
         */
        @NotBlank(message = "值对应的名称不能为空")
        @Size(max = 100, message = "值对应的名称最大长度不能超过100位")
        private String name;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 值对应的名称
         */
        @NotBlank(message = "值对应的名称不能为空")
        @Size(max = 100, message = "值对应的名称最大长度不能超过100位")
        private String name;

    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 左括号
         */
        @Size(max = 10, message = "左括号最大长度不能超过10位")
        private String leftBracket;

        /**
         * 条件的字段
         */
        @NotBlank(message = "条件的字段不能为空")
        @Size(max = 30, message = "条件的字段最大长度不能超过30位")
        private String field;

        private String fieldName;

        /**
         * 下拉逻辑关系
         */
        @NotBlank(message = "比较符不能为空")
        @Size(max = 30, message = "比较符最大长度不能超过30位")
        private String compare;

        /**
         * 对应的值
         */
        @NotBlank(message = "对应的值不能为空")
        @Size(max = 30, message = "对应的值最大长度不能超过30位")
        private String value;

        /**
         * 右括号
         */
        @Size(max = 10, message = "右括号最大长度不能超过10位")
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and
         */
        @StateEnumValue(strValues = {"or", "and"}, message = "逻辑关系有误")
        private String logic;

        /**
         * 序号
         */
        private Integer index;

    }


}