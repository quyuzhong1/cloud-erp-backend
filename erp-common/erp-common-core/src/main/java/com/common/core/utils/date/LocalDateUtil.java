package com.common.core.utils.date;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Cloud
 */
@Slf4j
public class LocalDateUtil {

    /**
     * LocalDate转Date
     *
     * @param localDate
     * @return
     */
    public static Date localDate2Date(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());

    }

    /**
     * Date转LocalDate
     *
     * @param date
     */
    public static LocalDate date2LocalDate(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    }

    /**
     * Date转换为LocalDateTime
     *
     * @param date
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }


    /**
     * LocalDateTime转换为Date
     *
     * @param localDateTime
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        return date;
    }


    /**
     * 获取当天开始时间
     *
     * @param localDate
     */
    public static LocalDateTime startLocalDateTime(LocalDate localDate) {
        LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);
        return startTime;
    }

    /**
     * 获取当天开始时间
     *
     * @param localDate
     */
    public static LocalDateTime endLocalDateTime(LocalDate localDate) {
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        return endTime;
    }


    /**
     * 获取环比日期
     *
     * @return
     */
    public static LocalDateTime getRingRatioDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate != null && startDate != null) {
            long diff = Duration.between(startDate, endDate).toDays();
            return LocalDateTime.of(startDate.minusDays(diff).toLocalDate(), LocalTime.MIN);
        }
        return null;
    }


    /**
     * 获取本周开始时间
     *
     * @return
     */
    public static LocalDateTime getThisWeekStart(LocalDate date) {
        LocalDateTime WeekStart = LocalDateTime.of(date.with(DayOfWeek.MONDAY), LocalTime.MIN);
        return WeekStart;
    }


    /**
     * 获取本周结束时间
     *
     * @return
     */
    public static LocalDateTime getThisWeekEnd(LocalDate date) {
        LocalDateTime weekEnd = LocalDateTime.of(date.with(DayOfWeek.SUNDAY), LocalTime.MAX);
        return weekEnd;
    }

    /**
     * 获取上周开始时间
     *
     * @return
     */
    public static LocalDateTime getLastWeekStart(LocalDate date) {
        LocalDateTime WeekStart = LocalDateTime.of(date.with(DayOfWeek.MONDAY), LocalTime.MIN).plusWeeks(-1);
        return WeekStart;
    }

    /**
     * 获取上周结束时间
     *
     * @return
     */
    public static LocalDateTime getLastWeekEnd(LocalDate date) {
        LocalDateTime weekEnd = LocalDateTime.of(date.with(DayOfWeek.SUNDAY), LocalTime.MAX).plusWeeks(-1);
        return weekEnd;
    }


    /**
     * 获取本月开始时间
     *
     * @return
     */
    public static LocalDateTime getThisMonthStart(LocalDate date) {
        LocalDateTime monMin = LocalDateTime.of(date.with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
        return monMin;
    }


    /**
     * 获取本月结束时间
     *
     * @return
     */
    public static LocalDateTime getThisMonthEnd(LocalDate date) {
        LocalDateTime monMax = LocalDateTime.of(date.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
        return monMax;
    }

    /**
     * 获取上月开始时间
     *
     * @return
     */
    public static LocalDateTime getLastMonthStart(LocalDate date) {
        return LocalDateTime.of(date.minus(1L, ChronoUnit.MONTHS).with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
    }

    /**
     * 获取上月结束时间
     *
     * @return
     */
    public static LocalDateTime getLastMonthEnd(LocalDate date) {
        return LocalDateTime.of(date.minus(1L, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);

    }

    /**
     * 获取本季度开始时间
     *
     * @return
     */
    public static LocalDateTime getThisQuarterStart(LocalDate date) {
        Month month = Month.of(date.getMonth().firstMonthOfQuarter().getValue());
        return LocalDateTime.of(LocalDate.of(date.getYear(), month, 1), LocalTime.MIN);

    }


    /**
     * 获取本季度结束时间
     *
     * @return
     */
    public static LocalDateTime getThisQuarterEnd(LocalDate date) {
        Month month = Month.of(date.getMonth().firstMonthOfQuarter().getValue()).plus(2L);
        return LocalDateTime.of(LocalDate.of(date.getYear(), month, month.length(date.isLeapYear())), LocalTime.MAX);

    }

    /**
     * 获取上季度开始时间
     *
     * @return
     */
    public static LocalDateTime getLastQuarterStart(LocalDate date) {
        Month firstMonthOfQuarter = Month.of(date.getMonth().firstMonthOfQuarter().getValue());
        Month firstMonthOfLastQuarter = firstMonthOfQuarter.minus(3L);
        int yearOfLastQuarter = firstMonthOfQuarter.getValue() < 4 ? date.getYear() - 1 : date.getYear();
        return LocalDateTime.of(LocalDate.of(yearOfLastQuarter, firstMonthOfLastQuarter, 1), LocalTime.MIN);

    }

    /**
     * 获取上季度结束时间
     *
     * @return
     */
    public static LocalDateTime getLastQuarterEnd(LocalDate date) {
        Month firstMonthOfQuarter = Month.of(date.getMonth().firstMonthOfQuarter().getValue());
        Month firstMonthOfLastQuarter = firstMonthOfQuarter.minus(1L);
        int yearOfLastQuarter = firstMonthOfQuarter.getValue() < 4 ? date.getYear() - 1 : date.getYear();
        return LocalDateTime.of(LocalDate.of(yearOfLastQuarter, firstMonthOfLastQuarter, firstMonthOfLastQuarter.maxLength()), LocalTime.MAX);
    }

    /**
     * 导入接收后转为localDateTime
     */
    public static LocalDateTime stringToLocalDateTime(String strDate) {
        Date date = EnumTimePattern.parseDate(strDate);
       return LocalDateUtil.date2LocalDateTime(date);
    }

    /**
     * 导入接收后转为localDate
     */
    public static LocalDate stringToLocalDate(String strDate) {
        Date date = EnumTimePattern.parseDate(strDate);
        return LocalDateUtil.date2LocalDate(date);
    }

    /**
     * 获取本年开始时间
     *
     * @return
     */
    public static LocalDateTime getThisYearStart(LocalDate date) {
        return LocalDateTime.of(date.with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
    }


    /**
     * 获取本年结束时间
     *
     * @return
     */
    public static LocalDateTime getThisYearEnd(LocalDate date) {
        return LocalDateTime.of(date.with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);
    }

    /**
     * 获取上年开始时间
     *
     * @return
     */
    public static LocalDateTime getLastYearStart(LocalDate date) {
        return LocalDateTime.of(date.minus(1L, ChronoUnit.YEARS).with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
    }

    /**
     * 获取上年结束时间
     *
     * @return
     */
    public static LocalDateTime getLastYearEnd(LocalDate date) {
        return LocalDateTime.of(date.minus(1L, ChronoUnit.YEARS).with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);
    }

    /**
     * 获取多少天前 开始时
     *
     * @param nowTime
     * @param days
     * @return
     */
    public static LocalDateTime getBeforeStartTime(LocalDateTime nowTime, int days) {
        return LocalDateTime.of(nowTime.minus(days, ChronoUnit.DAYS).toLocalDate(), LocalTime.MIN);
    }

    /**
     * 获取开始时间
     *
     * @param localDateTime
     * @return
     */

    public static LocalDateTime getStartTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MIN);
        }
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    public static LocalDateTime getEndTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MAX);
        }
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }

    public static Integer countDaysForLocalDate(LocalDate startDate, LocalDate endDate, List<LocalDate> dateList) {
        endDate = endDate.plusDays(1);
        if (null == dateList) {
            return 0;
        }
        long diffDaysLong = endDate.toEpochDay() - startDate.toEpochDay();
        Integer diffDays = Integer.valueOf(String.valueOf(diffDaysLong));
        if (CollectionUtil.isEmpty(dateList)) {
            return diffDays;
        }
        AtomicReference<Integer> count = new AtomicReference<>(0);
        LocalDate finalEndDate = endDate.minusDays(1);
        dateList.stream().forEach(date -> {
            if (date.compareTo(startDate) >= 0 && date.compareTo(finalEndDate) <= 0) {
                count.getAndSet(count.get() + 1);
            }

        });
        return diffDays - count.get();
    }

    public static Map<String, LocalDate> relationshipLocalDate(String code, LocalDate startDate, LocalDate endDate, Integer intervalWorkPeriod, Integer planWorkPeriod, List<LocalDate> dateList) {
        LocalDate planEndDate = null;
        LocalDate planStartDate = null;
        switch (code) {
            case "fs":
                // 间隔工期
                planStartDate = endDate;
                // 跳过休息日
                planStartDate = getPlanWorkPeriodDate(intervalWorkPeriod + 1, dateList, planStartDate, 1, 0);
                planEndDate = planStartDate;
                planEndDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planEndDate, 1, 1);
                break;
            case "ss":
                planStartDate = startDate;
                planStartDate = getPlanWorkPeriodDate(intervalWorkPeriod, dateList, planStartDate, 1, 0);
                planEndDate = planStartDate;
                planEndDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planEndDate, 1, 1);
                break;
            case "sf":
                planEndDate = startDate;
                planEndDate = getPlanWorkPeriodDate(intervalWorkPeriod - 1, dateList, planEndDate, 1, 0);
                planStartDate = planEndDate;
                planStartDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planStartDate, -1, 1);
                break;
            default:
                planEndDate = endDate;
                planEndDate = getPlanWorkPeriodDate(intervalWorkPeriod, dateList, planEndDate, 1, 0);
                planStartDate = planEndDate;
                planStartDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planStartDate, -1, 1);
                break;
        }
        HashMap<String, LocalDate> hashMap = new HashMap<>(6);
        hashMap.put("startDate", planStartDate);
        hashMap.put("endDate", planEndDate);
        return hashMap;
    }

    private static LocalDate getPlanWorkPeriodDate(Integer planWorkPeriod, List<LocalDate> dateList, LocalDate planStartDate, Integer diffDay, Integer type) {
        if (planWorkPeriod < 0) {
            planStartDate = planStartDate.plusDays(planWorkPeriod);
            while (dateList.contains(planStartDate)){
                planStartDate = planStartDate.minusDays(diffDay);
            }
        }
        while (planWorkPeriod > type){
            planStartDate = planStartDate.plusDays(diffDay);
            if(!dateList.contains(planStartDate)){
                planWorkPeriod--;
            }
        }
        return planStartDate;
    }

    public static LocalDateTime strToLocalDateTime(String timeStr) {
        if (CharSequenceUtil.isBlank(timeStr)) {
            return null;
        }
        if(timeStr.contains("T")){
            timeStr = timeStr.replace("T"," ");
        }
        return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(DateUtil.fmt));

    }

    // 获取指定时间的指定格式
    public static String formatTime(LocalDateTime time, String pattern) {
        if (Objects.isNull(time)) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime plusHours(LocalDateTime startTime, String hourStr) {
        if (CharSequenceUtil.isBlank(hourStr)){
            return startTime;
        }
        BigDecimal hour = new BigDecimal(hourStr);
        int intValue = hour.intValue();
        LocalDateTime result = null;
        if(intValue >= 0){
            result = startTime.plusHours(intValue);
        }
        BigDecimal remainder = hour.remainder(BigDecimal.ONE);
        if(remainder.compareTo(BigDecimal.ZERO) > 0){
            result = result.plusMinutes(new BigDecimal(60).multiply(remainder).intValue());
        }
        return result;
    }

    /**
     * 转化日期
     *
     * @param dateStr
     * @return java.time.LocalDate
     * @author yl
     * @date 2023-10-24 15:06
     */
    public static LocalDate parseStrToLocalDate(String dateStr) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            }
            if (dateStr.contains("/")) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                return LocalDate.parse(dateStr, dateTimeFormatter);
            } else {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 转化日期
     *
     * @param dateStr
     * @return java.time.LocalDate
     * @author yl
     * @date 2023-10-24 15:06
     */
    public static LocalDateTime parseStrToLocalTime(String dateStr) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            }

            // 判断是否包含时间部分（HH:mm:ss）
            boolean hasTime = dateStr.contains(" ");
            DateTimeFormatter formatter;

            if (dateStr.contains("/")) {
                if (hasTime) {
                    formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");
                } else {
                    // 没有时间部分，补上 00:00:00 并解析
                    formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    return date.atStartOfDay(); // 返回当天 0 点
                }
            } else {
                if (hasTime) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                } else {
                    // 没有时间部分，补上 00:00:00 并解析
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    return date.atStartOfDay(); // 返回当天 0 点
                }
            }

            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            log.error("parseStrToLocalTime 出错了>>>{}", e);
        }
        return null;
    }

    /**
     * 检查两个时间段是否有时间重叠
     */
    public static boolean isOverlap (LocalDate realStartDate, LocalDate realEndDate,
                                      LocalDate startDate, LocalDate endDate) {
        return (realStartDate.compareTo(endDate) <=0 && startDate.compareTo(realEndDate) <= 0);
    }

    /**
     * 检查两个时间段是否有时间重叠
     */
    public static boolean isOverlapLocalDateTime (LocalDateTime realStartTime, LocalDateTime realEndTime,
                                                  LocalDateTime startTime, LocalDateTime endTime) {
        return (realStartTime.compareTo(endTime) <=0 && startTime.compareTo(realEndTime) <= 0);
    }

    public static LocalDateTime getStartDateTimeOfYear(int year) {
        return LocalDateTime.of(year, Month.JANUARY, 1, 0, 0, 0);
    }

    public static LocalDateTime getEndDateTimeOfYear(int year) {
        LocalDateTime endDateTime = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59, 59);
        return endDateTime.withNano(999_999_999); // Adjust nanoseconds to the maximum value
    }

    /**
     * 获取日期范围的每一天
     * @Author Luo_WG
     * @Date 2024/10/29 17:46
     * @param startTime
     * @param endTime
     * @return java.util.List<java.lang.String>
     **/
    public static List<String> getDateDayList(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> dateList = new ArrayList<>();

        LocalDateTime startDate = startTime.toLocalDate().atStartOfDay();
        LocalDateTime endDate = endTime.toLocalDate().atStartOfDay();

        LocalDateTime currentDate = startDate;

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            dateList.add(currentDate.format(formatter));
            currentDate = currentDate.plusDays(1);
        }

        return dateList;
    }

    /**
     * 计算两个时间之间的小时数，保留小数
     * @param startTime
     * @param endTime
     * @param decimalPlaces 小数位数
     * @return
     */
    public static double calculateHoursWithDecimal(LocalDateTime startTime, LocalDateTime endTime, int decimalPlaces) {
        Duration duration = Duration.between(startTime, endTime);
        double totalHours = duration.toMillis() / 1000.0 / 3600.0;
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(totalHours * scale) / scale;
    }
    /**
     * 计算两个时间之间的秒数，结果为整数
     * @param startTime
     * @param endTime
     * @return 秒数
     */
    public static long calculateSeconds(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.until(endTime, ChronoUnit.SECONDS);
    }
}

