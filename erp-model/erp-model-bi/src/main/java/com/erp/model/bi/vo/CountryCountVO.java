package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname CountryCountVO
 * @Description TODO
 * @Date 2022-12-26 15:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CountryCountVO implements Serializable {

    /**
     * 订单统计
     */
    private Integer orderCount;


    private String name;
}
