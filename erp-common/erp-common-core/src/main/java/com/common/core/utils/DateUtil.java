package com.common.core.utils;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Classname 日期工具类
 * @Description TODO
 * @Date 2022-08-02 16:10
 * @Created by yl
 */
public class DateUtil {

    private DateUtil() {

    }

    private static final String fmt = "yyyy-MM-dd HH:mm:ss", fmt_day = "yyyy-MM-dd", fmt_recent = "MM-dd HH:mm", fmt_num = "yyMMdd", fmt_year = "yy", fmt_md = "MMdd";

    public static LocalDateTime nowDay() {
        return now(fmt_day);
    }

    public static LocalDateTime now(String fmt) {
        return toDay(LocalDateTime.now());
    }

    public static LocalDateTime toDay(LocalDateTime date) {
        String str = date.format(DateTimeFormatter.ofPattern(fmt_day));
        return LocalDateTime.parse(str + " 00:00:00", DateTimeFormatter.ofPattern(fmt));
    }

    //获取 日期的中文名字
    public static String getCnDate(LocalDate date) {
        int dayOfYear = date.getYear();
        int dayOfMonth = date.getDayOfMonth();
        int monthValue = date.getMonthValue();
        StringBuffer sb=new StringBuffer();
        sb.append(dayOfYear).append("年");
        sb.append(monthValue).append("月");
        sb.append(dayOfMonth).append("日");
        return sb.toString();
    }

}
