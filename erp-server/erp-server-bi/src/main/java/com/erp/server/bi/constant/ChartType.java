package com.erp.server.bi.constant;

/**
 * @Classname ChartType
 * @Date 2022-12-21 9:19
 * @Created by yl
 */
public class ChartType {
    // 私有构造函数，防止实例化
    private ChartType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 饼图
     */
    public static final String PIE = "pie";

    /**
     * 柱状图
     */
    public static final String BAR = "bar";

    /**
     * 折线图
     */
    public static final String LINE = "line";
}
