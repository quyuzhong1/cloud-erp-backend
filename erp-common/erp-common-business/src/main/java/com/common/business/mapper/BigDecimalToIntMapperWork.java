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

    /**
     * 长度(cm)转整型：向上取整，避免 9.9 截成 9、0.5 截成 0 导致物流平台校验失败
     */
    @Named("bigDecimalCmToIntCeil")
    public Integer bigDecimalCmToIntCeil(BigDecimal val) {
        if (val == null || val.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return val.setScale(0, RoundingMode.CEILING).intValue();
    }
}
