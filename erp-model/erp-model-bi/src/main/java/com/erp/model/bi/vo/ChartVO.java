package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname ChartVO
 * @Description TODO
 * @Date 2022-12-15 14:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChartVO<T> implements Serializable {

    private List<String> xAxis;     //表头

    //有多少个
    private List<SeriesVO<T>> series; //数据

}
