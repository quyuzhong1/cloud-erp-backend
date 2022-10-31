package com.common.core.excel;/**
 * 版权所有(C) 2021 广东省宏博伟智技术有限公司
 * 创建:QuYuZhong 2021/2/23
 */

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.DateUtils;

import java.sql.Date;


/**
 * @ClassName SqlDateConverter
 * @Description SqlDate与String转换器
 * @Author Quyuzhong
 * @Date 2021/2/23
 */
public class SqlDateStringConverter implements Converter<Date> {
	@Override
	public Class supportJavaTypeKey() {
		return Date.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Date convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
			return (Date) DateUtils.parseDate(cellData.getStringValue(), contentProperty.getDateTimeFormatProperty().getFormat());
		} else {
			return (Date) DateUtils.parseDate(cellData.getStringValue(), (String) null);
		}
	}

	@Override
	public CellData convertToExcelData(Date value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
			return new CellData(DateUtils.format(value, contentProperty.getDateTimeFormatProperty().getFormat()));
		} else {
			return new CellData(DateUtils.format(value, "yyyy-MM-dd"));
		}
	}
}
