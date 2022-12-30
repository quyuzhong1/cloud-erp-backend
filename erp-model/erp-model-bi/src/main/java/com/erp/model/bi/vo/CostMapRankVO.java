package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2022/12/30 14:30
 **/

@Data
@NoArgsConstructor
public class CostMapRankVO {

    private String key;

    private BigDecimal value;

    private Integer rank;
}
