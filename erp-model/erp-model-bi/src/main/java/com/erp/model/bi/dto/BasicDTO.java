package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname BasicDTO

 * @Date 2022-10-25 19:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicDTO implements Serializable {

    /**
     * ID
     */
    private String id;

    /**
     * name
     */
    private String name;

    /**
     * 是否引用
     */
    private BigDecimal sales;

}
