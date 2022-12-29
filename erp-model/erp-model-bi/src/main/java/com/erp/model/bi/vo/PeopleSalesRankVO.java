package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname PeopleSalesRankVO
 * @Description TODO
 * @Date 2022-12-27 11:04
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PeopleSalesRankVO implements Serializable {


    /**
     * 排名
     */
    private Integer ranking;


    /**
     * 名字
     */
    private String userName="无";


    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     * 上一次排名
     */
    private Integer lastRanking=0;


    /**
     * 环比率
     */
    private BigDecimal chainRelativeRatio=BigDecimal.ZERO;
}
