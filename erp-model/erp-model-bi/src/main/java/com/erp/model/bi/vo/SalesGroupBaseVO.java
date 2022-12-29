package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname SalesGroupBaseVO
 * @Description TODO
 * @Date 2022-12-26 10:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesGroupBaseVO implements Serializable {


    private String flagName;


    private BigDecimal sales;
}
