package com.erp.model.bi.vo;

import com.erp.common.business.vo.ChartVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统计数据共同的vo
 * @Classname
 * @Description TODO
 * @Date 2022-12-15 14:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StatisticalDataVO implements Serializable {

    /**
     *  标题名
     */
    private String name;

    /**
     *  bar代表柱状图，line代表线状图，pie代表圆形图
     */
    private String chartType;

    private ChartVO data;         //存放表头和表值
    /**
     * //总数量
     */
    private String sumNumber;   

}
