package com.common.core.utils.date;


import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
//import org.joda.time.format.DateTimeFormatter;

/**
 * @Classname 日期工具类
 * @Description TODO
 * @Date 2022-08-02 16:10
 * @Created by yl
 */
public class DateUtil {

    private DateUtil() {

    }

    public static final String fmt = "yyyy-MM-dd HH:mm:ss", fmt_day = "yyyy-MM-dd", fmt_recent = "MM-dd HH:mm", fmt_num = "yyMMdd", fmt_year = "yy", fmt_md = "MMdd";

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
    public static String getCnDate(LocalDate date) {
        int dayOfYear = date.getYear();
        int dayOfMonth = date.getDayOfMonth();
        int monthValue = date.getMonthOfYear();
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
     * 对日期的【秒】进行加/减
     *
     * @param date    日期
     * @param seconds 秒数，负数为减
     * @return 加/减几秒后的日期
     */
    public static Date addDateSeconds(Date date, int seconds) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusSeconds(seconds).toDate();
    }

    /**
     * 对日期的【分钟】进行加/减
     *
     * @param date    日期
     * @param minutes 分钟数，负数为减
     * @return 加/减几分钟后的日期
     */
    public static Date addDateMinutes(Date date, int minutes) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMinutes(minutes).toDate();
    }

    /**
     * 对日期的【小时】进行加/减
     *
     * @param date  日期
     * @param hours 小时数，负数为减
     * @return 加/减几小时后的日期
     */
    public static Date addDateHours(Date date, int hours) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusHours(hours).toDate();
    }

    /**
     * 对日期的【天】进行加/减
     *
     * @param date 日期
     * @param days 天数，负数为减
     * @return 加/减几天后的日期
     */
    public static Date addDateDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }

    /**
     * 对日期的【周】进行加/减
     *
     * @param date  日期
     * @param weeks 周数，负数为减
     * @return 加/减几周后的日期
     */
    public static Date addDateWeeks(Date date, int weeks) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusWeeks(weeks).toDate();
    }

    /**
     * 对日期的【月】进行加/减
     *
     * @param date   日期
     * @param months 月数，负数为减
     * @return 加/减几月后的日期
     */
    public static Date addDateMonths(Date date, int months) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMonths(months).toDate();
    }

    /**
     * 对日期的【年】进行加/减
     *
     * @param date  日期
     * @param years 年数，负数为减
     * @return 加/减几年后的日期
     */
    public static Date addDateYears(Date date, int years) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusYears(years).toDate();
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
     * 计算两个日期相隔月份数
     *
     * @param date1 <String>
     * @param date2 <String>
     * @return int
     */
    public static int getDiffMonth(Date date1, Date date2) {
        int result = 0;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(date1);
        c2.setTime(date2);

        if (c1.getTime().after(c2.getTime())) {
            throw new RuntimeException("[起始时间]不能大于[结束时间]");
        }

        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
            Date date3 = getLastDayOfYear(c1.getTime()).getTime();
            Date date4 = addDateSeconds(date3, 1);

            result = getDiffMonth(date1, date3) + 1 + getDiffMonth(date4, date2);
        } else {
            result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        }


        return result == 0 ? 1 : Math.abs(result);
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


    public static LocalDate getCurrentTime() {

        return LocalDate.now();
    }


    public static String conversionDate(Date date, String fmt) {
        if (date != null) {
            if (StringUtils.isBlank(fmt)) {
                fmt = DateUtil.fmt_day;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date.getTime());
        }
        return "";
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


}
