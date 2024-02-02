package com.common.business.mapper;

import org.mapstruct.Named;

import java.math.BigDecimal;
import java.text.ParseException;

/**
 * big 转int
 *@author yl
 *@date 2023-11-28
 */

@Named("BigDecimalMapperWork")
public class BigDecimalMapperWork {


    @Named("bigDecimalToInt")
    public Integer bigDecimalToInt(BigDecimal val) throws ParseException {
        if (val == null){
            return 0;
        }
        return val.intValue();
    }

    @Named("bigDecimalToStr")
    public String bigDecimalToStr(BigDecimal val) throws ParseException {
        if (val == null){
            return "";
        }
        return val.toString();
    }
}
