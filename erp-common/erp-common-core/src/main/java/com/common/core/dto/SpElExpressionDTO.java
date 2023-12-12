package com.common.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname SpElExpressionDTO
 * @Description TODO
 * @Date 2023-12-04 16:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SpElExpressionDTO implements Serializable {

    /**
     *  表达式
     */
    private String expression;


    /**
     * 需要集合的字段
     */
    private List<SpElAddFieldDTO>  spElAddFieldList;



}
