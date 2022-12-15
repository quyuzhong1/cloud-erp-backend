package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname SeriesVO
 * @Description TODO
 * @Date 2022-12-15 14:50
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SeriesVO<T> {

    private String name;    //当前图名
    private List<T> data;   //数据
    private String stack;
}
