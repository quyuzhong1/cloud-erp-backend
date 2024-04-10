package com.common.business.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author Lambda
 * @Classname LocalDateStringConverter
 * @Date 2023-03-22 16:46
 * @Created by yl
 */
public class LocalDateStringConverter implements Converter<LocalDateTime> {
    @Override
    public Class supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDateTime convertToJavaData(CellData cellData, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        if (cellData.getType().equals(CellDataTypeEnum.NUMBER)) {
            // Excel中的日期时间值是基于1900年1月1日的序列值，加上数值即可转换为Java的LocalDateTime
            // 时间值是小数部分，代表一天中的时间，乘以86400（秒数）可转换为秒数
            double num = cellData.getNumberValue().doubleValue();
            long secondsSinceEpoch = (long) ((num - 25569) * 86400);
            return LocalDateTime.ofEpochSecond(secondsSinceEpoch, 0, ZoneOffset.UTC);
        }
        return LocalDateTime.parse(cellData.getStringValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public CellData convertToExcelData(LocalDateTime localDateTime, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String format = formatter.format(localDateTime);
        return new CellData(format);

    }
}
