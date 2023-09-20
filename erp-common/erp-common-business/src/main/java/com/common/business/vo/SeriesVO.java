package com.common.business.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname SeriesVO

 * @Date 2022-12-15 14:50
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SeriesVO<T> {
    //当前图名
    private String name;
    //当前图类型 line为折线， bar为柱状
    private String type;
    //数据
    private List<T> data;


}
