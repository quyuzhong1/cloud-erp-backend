package com.common.core.utils.date;

import cn.hutool.core.collection.CollectionUtil;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Cloud
 */
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
            return LocalDateTime.of(startDate.minusDays(diff).toLocalDate(),LocalTime.MIN) ;
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
     * @param localDateTime
     * @return
     */

    public static LocalDateTime getStartTime(LocalDateTime localDateTime) {
        if(localDateTime!=null){
            return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MIN);
        }
        return LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
    }

    public static LocalDateTime getEndTime(LocalDateTime localDateTime) {
        if(localDateTime!=null){
            return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MAX);
        }
        return LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
    }

    public static Integer countDaysForLocalDate(LocalDate startDate, LocalDate endDate, List<LocalDate> dateList){
        endDate = endDate.plusDays(1);
        if (null == dateList) {
            return 0;
        }
        long diffDaysLong = endDate.toEpochDay() - startDate.toEpochDay();
        Integer diffDays = Integer.valueOf(String.valueOf(diffDaysLong));
        if(CollectionUtil.isEmpty(dateList)){
            return diffDays;
        }
        AtomicReference<Integer> count = new AtomicReference<>(0);
        LocalDate finalEndDate = endDate.minusDays(1);
        dateList.stream().forEach(date -> {
            if(date.compareTo(startDate) >= 0 && date.compareTo(finalEndDate) <= 0){
                count.getAndSet(count.get() + 1);
            }

        });
        return diffDays - count.get();
    }

    public static Map<String, LocalDate> relationshipLocalDate(String code,LocalDate startDate, LocalDate endDate, Integer intervalWorkPeriod, Integer planWorkPeriod, List<LocalDate> dateList){
        LocalDate planEndDate = null;
        LocalDate planStartDate = null;
        switch (code){
            case "fs":
                // 间隔工期
                planStartDate = endDate.plusDays(intervalWorkPeriod + 1);
                // 跳过休息日
                while (dateList.contains(planStartDate)){
                    planStartDate = planStartDate.plusDays(1);
                }
                planEndDate = planStartDate;
                planEndDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planEndDate, 1);
                break;
            case "ss":
                planStartDate = startDate.plusDays(intervalWorkPeriod);
                while (dateList.contains(planStartDate)){
                    planStartDate = planStartDate.plusDays(1);
                }
                planEndDate = planStartDate;
                planEndDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planEndDate, 1);
                break;
            case "sf":
                planEndDate = startDate.plusDays(intervalWorkPeriod - 1);
                while (dateList.contains(planEndDate)){
                    planEndDate = planEndDate.plusDays(-1);
                }
                planStartDate = planEndDate;
                planStartDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planStartDate, -1);
                break;
            default:
                planEndDate = endDate.plusDays(intervalWorkPeriod);
                while (dateList.contains(planEndDate)){
                    planEndDate = planEndDate.plusDays(-1);
                }
                planStartDate = planEndDate;
                planStartDate = getPlanWorkPeriodDate(planWorkPeriod, dateList, planStartDate, -1);
                break;
        }
        HashMap<String, LocalDate> hashMap = new HashMap<>(6);
        hashMap.put("startDate", planStartDate);
        hashMap.put("endDate", planEndDate);
        return hashMap;
    }

    private static LocalDate getPlanWorkPeriodDate(Integer planWorkPeriod, List<LocalDate> dateList, LocalDate planStartDate, Integer diffDay) {
        while (planWorkPeriod > 1){
            planStartDate = planStartDate.plusDays(diffDay);
            if(!dateList.contains(planStartDate)){
                planWorkPeriod--;
            }
        }
        return planStartDate;
    }
}

