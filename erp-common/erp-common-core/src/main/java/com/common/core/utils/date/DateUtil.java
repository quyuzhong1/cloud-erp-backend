package com.common.core.utils.date;


import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @Classname 日期工具类
 * @Description TODO
 * @Date 2022-08-02 16:10
 * @Created by yl
 */
public class DateUtil {

    private DateUtil() {

    }

    public static final String fmt = "yyyy-MM-dd HH:mm:ss", fmt_day = "yyyy-MM-dd", fmt_year_month = "yyyy/MM/dd", fmt_recent = "MM-dd HH:mm", fmt_num = "yyMMdd", fmt_year = "yy", fmt_md = "MMdd", fmt_hms = "HH:mm:ss";

    public final static String FMT_YEAR4 = "yyyy", DATE_TIME_PATTERN_NO_SEC = "yyyy-MM-dd HH:mm", DATE_PATTERN_SHORT_YEAR_NO_SP = "yyyyMMdd";


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
    public static String getCnDate(LocalDateTime date) {
        int dayOfYear = date.getYear();
        int dayOfMonth = date.getDayOfMonth();
        int monthValue = date.getMonthValue();
        StringBuffer sb = new StringBuffer();
        sb.append(dayOfYear).append("年");
        sb.append(monthValue).append("月");
        sb.append(dayOfMonth).append("日");
        return sb.toString();
    }

    /**
     * 获取某天天开始时间
     *
     * @return
     */
    public static Date getStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取某天  结束时间
     *
     * @return
     */
    public static Date getEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 对日期的【天】进行加/减
     *
     * @param date 日期
     * @param days 天数，负数为减
     * @return 加/减几天后的日期
     */
    public static Date addDateDays(Date date, int days) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 把日期往后增加一天,整数  往后推,负数往前移动
        calendar.add(Calendar.DATE, days);
        // 这个时间就是日期往后推一天的结果
        return calendar.getTime();
    }


    /**
     * 对日期的【年】进行加/减
     *
     * @param date  日期
     * @param years 年数，负数为减
     * @return 加/减几年后的日期
     */
    public static Date addDateYears(Date date, int years) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 把日期往后增加一年,整数  往后推,负数往前移动
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * 设置cal的最大时间
     *
     * @param cal
     */
    public static void setMaxTimeForDay(Calendar cal) {
        if (cal == null) {
            return;
        }

        // 设置每天的最大小时
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        // 设置每小时最大分钟
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        // 设置每分钟最大秒
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        // 设置最大毫秒数
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
    }

    /**
     * 获取指定日期 所属年的最后一天
     *
     * @param date
     * @return
     */
    public static Calendar getLastDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        setMaxTimeForDay(cal);
        return cal;
    }


    /**
     * <li>功能描述：时间相减得到天数
     *
     * @param beginDateStr
     * @param endDateStr
     * @return long
     * @author Administrator
     */
    public static long getDiffDay(String beginDateStr, String endDateStr) {
        long day = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = null;
        Date endDate = null;

        try {
            beginDate = format.parse(beginDateStr);
            endDate = format.parse(endDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return getDiffDay(beginDate, endDate);
    }

    public static int getDiffDay(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000));
    }

    /**
     * 计算某年某周的开始日期
     * <p>
     * 格式 yyyy 1到52或者53
     *
     * @return 日期，格式为yyyy-MM-dd
     */
    public static String getWeekStartDate(int yearNo, int weekNo) {
        SimpleDateFormat format_Date = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(7);
        cal.set(Calendar.YEAR, yearNo);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);

        return format_Date.format(cal.getTime());
    }

    /**
     * 计算某年某周的结束日期
     * <p>
     * 格式 yyyy 1到52或者53
     *
     * @return 日期，格式为yyyy-MM-dd
     */
    public static String getWeekEndDate(int yearNo, int weekNo) {
        SimpleDateFormat format_Date = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // 第几天
        cal.setMinimalDaysInFirstWeek(7);
        cal.set(Calendar.YEAR, yearNo);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);

        return format_Date.format(cal.getTime());
    }

    /**
     * 获取当前日期是多少年多少周
     *
     * @param date 日期
     * @return 例如：201925 2019年的第25周
     */
    public static String getYearWeek(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int year = instance.get(Calendar.YEAR);
        int week = instance.get(Calendar.WEEK_OF_YEAR);
        if (week == 1 && instance.get(Calendar.MONTH) == Calendar.DECEMBER) {
            year++;
        }
        return year + (week < 10 ? "0" + week : String.valueOf(week));
    }

    public static String getWeekStartDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        cal.add(Calendar.DATE, (1 - dayWeek)); // 本周第一天

        return sdf.format(cal.getTime());
    }

    public static String getWeekEndDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        cal.add(Calendar.DATE, 7 - dayWeek);
        return sdf.format(cal.getTime());
    }

    /**
     * 字符串转换成日期,,格式 yyyy-MM-dd
     *
     * @param strDate 日期字符串,格式：Date
     */
    public static Date stringToDate(String strDate) {
        return EnumTimePattern.parseDate(strDate);
    }


    public static LocalDateTime getCurrentTime() {

        return LocalDateTime.now();
    }


    public static String conversionDate(Date date, String fmt) {
        if (date != null) {
            if (StringUtils.isBlank(fmt)) {
                fmt = DateUtil.fmt_day;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date.getTime());
        }
        return "-";
    }


    /**
     * 获取两个时间相差多少
     *
     * @param endDate
     * @param startDate
     * @return java.lang.String
     * @author yl
     * @date 2022-08-18 14:45
     */
    public static String discrepancy(Date endDate, Date startDate) {
        if (endDate != null && startDate != null) {
            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = endDate.getTime() - startDate.getTime();
            // 计算差多少天
            long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            // 计算差多少分钟
            long min = diff % nd % nh / nm;
            // 计算差多少秒//输出结果
            long sec = diff % nd % nh % nm / ns;
            return day + "天" + hour + "小时" + min + "分钟" + sec + "秒";
        }
        return "";
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currYearLast = calendar.getTime();
        return currYearLast;
    }

    /**
     * 获取环比日期
     *
     * @param endDate
     * @param startDate
     * @return java.lang.String
     */
    public static String getRingRatioDate(Date endDate, Date startDate) {
        if (endDate != null && startDate != null) {
            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = endDate.getTime() - startDate.getTime();
            // 计算差多少天
            long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            // 计算差多少分钟
            long min = diff % nd % nh / nm;
            // 计算差多少秒//输出结果
            long sec = diff % nd % nh % nm / ns;


            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);
            calendar.add(Calendar.DATE, Integer.valueOf(-day + ""));
            calendar.add(Calendar.HOUR, Integer.valueOf(-hour + ""));
            calendar.add(Calendar.MINUTE, Integer.valueOf(-min + ""));
            calendar.add(Calendar.SECOND, Integer.valueOf(-sec + ""));
            //设置时间格式
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = f.format(calendar.getTime());
            return date;
        }
        return "";
    }

    /**
     * Z字符串转日期
     *
     * @param str
     * @return
     */
    public static Date strToDate(String str, String fmt) {
        if (StringUtils.isNotBlank(str)) {
            SimpleDateFormat format = new SimpleDateFormat(fmt);
            Date date = null;
            try {
                date = format.parse(str);
            } catch (ParseException e) {
                return date;
            }
            return date;
        }
        return null;

    }

}
