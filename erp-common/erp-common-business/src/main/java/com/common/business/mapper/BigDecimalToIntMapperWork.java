package com.common.business.mapper;

import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * big 转int
 *@author yl
 *@date 2023-11-28
 */
@Named("BigDecimalToIntMapperWork")
public class BigDecimalToIntMapperWork {


    @Named("bigDecimalToInt")
    public Integer bigDecimalToInt(BigDecimal val) throws ParseException {
        if (val == null){
            return 0;
        }
        return val.intValue();
    }
}
