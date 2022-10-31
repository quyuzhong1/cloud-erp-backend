package com.common.core.excel;/**
 * 版权所有(C) 2021 广东省宏博伟智技术有限公司
 * 创建:QuYuZhong 2021/2/23
 */

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;


/**
 * @ClassName SqlDateConverter
 * @Description SqlDate与数字转换器
 * @Author Quyuzhong
 * @Date 2021/2/23
 */
public class SqlDateNumberConverter implements Converter<Date> {
	@Override
	public Class supportJavaTypeKey() {
		return Date.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.NUMBER;
	}

	@Override
	public Date convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
			return (Date) DateUtil.getJavaDate(cellData.getNumberValue().doubleValue(), contentProperty.getDateTimeFormatProperty().getUse1904windowing(), (TimeZone) null);
		} else {
			return (Date) DateUtil.getJavaDate(cellData.getNumberValue().doubleValue(), globalConfiguration.getUse1904windowing(), (TimeZone) null);
		}
	}

	@Override
	public CellData convertToExcelData(Date value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
			return new CellData(BigDecimal.valueOf(DateUtil.getExcelDate(value, contentProperty.getDateTimeFormatProperty().getUse1904windowing())));
		} else {
			return new CellData(BigDecimal.valueOf(DateUtil.getExcelDate(value, globalConfiguration.getUse1904windowing())));
		}
	}
}
