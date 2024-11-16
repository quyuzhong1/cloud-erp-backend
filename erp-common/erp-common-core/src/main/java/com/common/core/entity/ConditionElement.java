package com.common.core.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yl
 * @Date 2023-09-04 10:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionElement {

    /**
     * 左括号
     */

    private String leftBracket;

    /**
     * 对应字段
     */
    private String field;


    /**
     * 选项逻辑关系 大于 等于 等等
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
     * 逻辑关系 and 或者or
     */
    private String logic;

    /**
     * 对应的值类型
     */
    private String valueType;





}
