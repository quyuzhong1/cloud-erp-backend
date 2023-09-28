package com.common.business.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname ChartVO

 * @Date 2022-12-15 14:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChartVO<T> implements Serializable {
    /**
     * 表头
     */
    private List<String> xAxis;

    /**
     * 数据有多个
     */
    private List<SeriesVO<T>> series;

}
