package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 销售额目标分析
 *
 * @Author Cloud
 * @Date 2022/12/21 11:34
 **/
@Data
@NoArgsConstructor
public class TargetAnalysisVO<T> {

    /**
     * 列表
     */
    private List<T> list;

    /**
     * 年度销售额数据
     */
    private Map<String, BigDecimal> yearSalesTarget;
}
