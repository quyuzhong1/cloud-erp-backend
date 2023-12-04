package com.common.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname SpElAddFieldDTO
 * @Description 需要添加的字段
 * @Date 2023-12-04 16:47
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SpElAddFieldDTO implements Serializable {

    /**
     * 需要添加的字段
     */
    private String needAddField;

    /**
     * 原始的字段
     */
    private String originalField;
}
