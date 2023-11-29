package com.common.business.mapper;

import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
}
