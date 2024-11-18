package com.common.business.mapper;

import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author liuruipeng
 */
@Named("ObjectMapperWork")
public class ObjectMapperWork {

    @Named("objToString")
    public String objToString(Object object) {
        return Objects.nonNull(object)? String.valueOf(object) : null;
    }
    @Named("objToBigDecimal")
    public BigDecimal objToBigDecimal(Object obj) {
        BigDecimal bigDecimal = null;
        if (obj instanceof BigDecimal) {
            bigDecimal = (BigDecimal) obj;
        } else if (obj instanceof String) {
            bigDecimal = new BigDecimal((String) obj);
        } else if (obj instanceof Number) {
            bigDecimal = BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        return bigDecimal;
    }
}
