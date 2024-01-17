package com.common.business.mapper;

import com.common.core.utils.MathUtil;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * @author liuruipeng
 * 数字处理mapper
 */
@Named("NumberMapperWork")
public class NumberMapperWork {


    @Named("divideByOneThousandWithThreeDecimal")
    public Number divideByOneThousandWithThreeDecimal(Integer val) throws ParseException {
        if (val == null){
            return null;
        }
        // 将Integer转换为BigDecimal
        BigDecimal bdInput = new BigDecimal(val);

        // 除以1000并四舍五入保留三位小数
        BigDecimal result = bdInput.divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);

        return result;
    }
    @Named("intToStr")
    public String intToStr(Integer val) {
        if (val == null){
            return "";
        }
        return String.valueOf(val);
    }

    @Named("gToKgByInt")
    public String gToKgByInt(Integer val) {
        if (val == null){
            return "";
        }
        BigDecimal weightKg= MathUtil.divide(new BigDecimal(val.toString()),new BigDecimal("1000"),3);
        return String.valueOf(weightKg);
    }



}
