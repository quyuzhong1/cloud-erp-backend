package com.common.business.mapper;

import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author liuruipeng
 */
@Named("DateMapperWork")
public class DateMapperWork {

    @Named("localDateTimeToDate")
    public Date toBooleanByYN(LocalDateTime localDateTime) {
        if(Objects.isNull(localDateTime)){
            return null;
        }
        // 转换为 Date
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Named("toStrByDate")
    public String toStrByDate(LocalDateTime localDateTime) {
        if(Objects.isNull(localDateTime)){
            return null;
        }
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化为字符串
        return localDateTime.format(formatter);
    }
}
