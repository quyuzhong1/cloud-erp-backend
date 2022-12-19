package com.common.core.utils.date;

import java.time.*;
import java.util.Date;

/**
 * @author Cloud
 */
public class LocalDateUtil {

    /**
     * LocalDate转Date
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
     * @param date
     */
    public static LocalDateTime date2LocalDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
      return localDateTime;
    }

    /**
     * LocalDateTime转换为Date
     * @param localDateTime
     */
    public static Date localDateTime2Date( LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
       return date;
    }


    /**
     * 获取当天开始时间
     * @param localDate
     */
    public static LocalDateTime  startLocalDateTime( LocalDate localDate){
        LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);
        return startTime;
    }

    /**
     * 获取当天开始时间
     * @param localDate
     */
    public static LocalDateTime  endLocalDateTime( LocalDate localDate){
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        return endTime;
    }
    
}

