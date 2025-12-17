package com.common.business.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月09日 15:51
 */
public class QueryUtils {

    /**
     * 拼接sql
     * @return
     */
    public static String splicingSQL(List<AdvanceQueryDTO> dtoList){
        if(CollectionUtils.isEmpty(dtoList)){
            return "1 = 1";
        }
        //将最后一个比较符去掉
        dtoList.get(dtoList.size()-1).setCompareSymbol("");
        StringBuilder sql = new StringBuilder();
        for(AdvanceQueryDTO dto : dtoList){
            QueryConditionEnum condEnum = QueryConditionEnum.CODE_MAPS.get(dto.getCompare());
            for(int i = 0; i<dto.getLeftBracketCount();i++){
                sql.append("(");
            }
            String contentSql;
            //为空处理为   (字段 = ''or 字段 is null)，不为空处理为  字段 != '' 其他直接拼接
            if (QueryConditionEnum.IS_NULL.equals(condEnum)) {
                sql.append( "(" + dto.getField() + " = '' or " + dto.getField() + " is null)").append(" ");
            } else if (QueryConditionEnum.NOT_NULL.equals(condEnum)) {
                sql.append( dto.getField() + " != ''").append(" ");
            } else {
                sql.append(dto.getField()).append(" ");
            }
            contentSql = QueryUtils.splicingCompareValueSQL(condEnum, dto);
            sql.append(contentSql).append(" ");

            sql.append(dto.getCompareSymbol()).append(" ");

            for(int i = 0; i<dto.getRightBracketCount();i++){
                sql.append(")");
            }

        }

        return sql.toString();
    }

    /**
     * 将比较符和值拼成sql  如 将 in [val1,val2] 拼成 in(val1,val2)
     * @param condEnum
     * @param dto
     * @return
     */
    public static String splicingCompareValueSQL(QueryConditionEnum condEnum, AdvanceQueryDTO dto){
        StringBuilder sql = new StringBuilder();
        if(QueryDataTypeEnum.NUMBER.getCode().equals(dto.getDataType()) && !StringUtils.isNumeric(dto.getValue().toString())){
            throw new ServiceException("数字类型的值必须为数字");
        }
        //starts_with 和 ends_with 处理成like，为空和不为空和between不处理
        if(QueryConditionEnum.STARTS_WITH.equals(condEnum) || QueryConditionEnum.ENDS_WITH.equals(condEnum) || QueryConditionEnum.CONTAINS.equals(condEnum)){
            sql.append("like").append(" ");
        }else if (QueryConditionEnum.NOT_CONTAINS.equals(condEnum)) {
        	sql.append("not like").append(" ");
        } else if (!QueryConditionEnum.IS_NULL.equals(condEnum) && !QueryConditionEnum.NOT_NULL.equals(condEnum) &&  !QueryConditionEnum.BETWEEN.equals(condEnum)){
            //日期格式的年月日小于等于 需要修改为小于，因为需要加一天
            String interval = QueryUtils.getDateStr(dto.getValue().toString());
            if(QueryDataTypeEnum.DATE.getCode().equals(dto.getDataType()) && QueryConditionEnum.LE.equals(condEnum)&& StringUtils.isNotBlank(interval)){
                sql.append(QueryConditionEnum.LT.getCode()).append(" ");
            }else{
                sql.append(condEnum.getCode()).append(" ");
            }
        }
        String val = "";
        //大于小于等于这种直接拼接值
        if(QueryConditionEnum.SET_DIRECT_VAL.contains(condEnum)){
            val = QueryUtils.handleVal(dto.getValue(),dto.getDataType(),condEnum);
        }
        //in 查询拼接成(val1,val2)格式
        if(QueryConditionEnum.SET_IN.contains(condEnum)){
            if (dto.getValue() instanceof Collection<?>) {
                Collection<Object> list = (Collection<Object>) dto.getValue();
                val = "(" + val;
                for(Object obj : list){
                    val = val + QueryUtils.handleVal(obj,dto.getDataType(),condEnum) + ",";
                }
                //去掉最后一个,
                val = val.substring(0, val.length() - 1);
                val = val+")";
            } else if(dto.getValue() instanceof String){
                val = " ( '" + dto.getValue() +"' )";
            }else {
                throw new ServiceException(ApiError.QUERY_LIST_TYPE_ERROR);
            }
        }

        //like查询拼接成 concat('%',#{val},'%') 或concat(#{val},'%') 或concat('%',#{val})
        if(QueryConditionEnum.SET_LIKE.contains(condEnum)){
            //_需要转义
            if(dto.getValue().toString().contains("_")){
                dto.setValue(dto.getValue().toString().replace("_","\\_"));
            }
            if(QueryConditionEnum.STARTS_WITH.equals(condEnum)){
                val = "LOWER('" + dto.getValue() + "%')";
            }else if(QueryConditionEnum.ENDS_WITH.equals(condEnum)){
                val = "LOWER('%" + dto.getValue() + "')";
            }else if(QueryConditionEnum.CONTAINS.equals(condEnum)){
                val = "LOWER('%" + dto.getValue() + "%')";
            }else if(QueryConditionEnum.NOT_CONTAINS.equals(condEnum)){
                val = "LOWER('%" + dto.getValue() + "%')";
            }else{
                val = "'%" + dto.getValue() + "%'";
            }
        }

        //between 拆成   >=  和 <=  传参是数组
        if(QueryConditionEnum.BETWEEN.equals(condEnum)){
            if(dto.getValue().getClass() != ArrayList.class){
                throw new ServiceException(ApiError.QUERY_LIST_TYPE_ERROR);
            }
            ArrayList<Object> list = (ArrayList<Object>) dto.getValue();
            if(list.size() < 2){
                throw new ServiceException(ApiError.QUERY_BETWEEN_ERROR);
            }
            String startDate = list.get(0).toString();
            String endDate = list.get(1).toString();
            if(DateUtil.isDateOrTimeValid(list.get(0).toString()) && DateUtil.isDateOrTimeValid(list.get(1).toString())){
                String interval = QueryUtils.getDateStr(endDate);
                if(StringUtils.isNotBlank(interval)){
                    val = " >= '"+formatChineseDateTime(startDate)+"' and " + dto.getField()+" < '"+getAddDate(endDate, interval)+"' ";
                }else{
                    val = " >= '"+formatChineseDateTime(startDate)+"' and " + dto.getField()+" < '"+formatChineseDateTime(endDate)+"' ";
                }
            }else{
                throw new ServiceException(ApiError.QUERY_ILLEGAL_DATE_FORMAT);
            }
        }
        sql.append(val);
        return sql.toString();
    }

    public static String handleVal(Object fieldVal,String dataType,QueryConditionEnum condEnum) {
        String result = fieldVal.toString();
        QueryDataTypeEnum queryDataTypeEnum = EnumMessage.getByCode(QueryDataTypeEnum.class,dataType);
        if(queryDataTypeEnum.equals(QueryDataTypeEnum.STRING)){
            result = "'"+ fieldVal + "'";
        }else if (queryDataTypeEnum.equals(QueryDataTypeEnum.DATE)){
            if(DateUtil.isDateOrTimeValid(fieldVal.toString())){
                String interval = QueryUtils.getDateStr(result);
                if(condEnum.equals(QueryConditionEnum.LE) && StringUtils.isNotBlank(interval)){
                    result = getAddDate(result, interval);
                }else{
                    result = formatChineseDateTime(result);
                }
                result = "'"+ result + "'";
            }else{
                throw new ServiceException(ApiError.QUERY_ILLEGAL_DATE_FORMAT);
            }
        }
        return result;
    }


    public static  String getDateStr(String input) {
        String[] patterns = {"yyyy", "yyyy-MM", "yyyy-MM-dd"};

        for (String pattern : patterns) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            try {
                if (pattern.equals("yyyy")) {
                    Year year = Year.parse(input, formatter);
                    return "YEAR";
                } else if (pattern.equals("yyyy-MM")) {
                    YearMonth yearMonth = YearMonth.parse(input, formatter);
                    return "MONTH";
                } else {
                    LocalDate date = LocalDate.parse(input, formatter);
                    return "DAY";
                }
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
    
    public static String getAddDate(String input , String interval) {
    	String formatChineseDateTime = formatChineseDateTime(input);
    	DateTime parse = cn.hutool.core.date.DateUtil.parse(formatChineseDateTime , "yyyy-MM-dd HH:mm:ss");
    	if("YEAR".equals(interval)) {
    		parse = cn.hutool.core.date.DateUtil.offsetMonth(parse, 12);
    	}else if("MONTH".equals(interval)) {
    		parse = cn.hutool.core.date.DateUtil.offsetMonth(parse, 1);
    	}else if("DAY".equals(interval)) {
    		parse = cn.hutool.core.date.DateUtil.offsetDay(parse, 1);
    	}
    	return cn.hutool.core.date.DateUtil.format(parse , "yyyy-MM-dd HH:mm:ss");
    }
    
    public static String formatChineseDateTime(String dateTime) {
    	String[] patterns = {"yyyy-MM-dd HH:mm:ss" , "yyyy-MM-dd" , "yyyy-MM" , "yyyy" };

        for (String pattern : patterns) {
        	DateTime parse = null;
            try {
				parse = cn.hutool.core.date.DateUtil.parse(dateTime , pattern);
			} catch (Exception e) {
			}
            if(parse != null) {
            	return cn.hutool.core.date.DateUtil.format(parse, "yyyy-MM-dd HH:mm:ss");
            }
        }
        return dateTime;
    }

    public static String listToStringValue(List<?> list, QueryDataTypeEnum dataTypeEnum){
        if (CollectionUtils.isEmpty(list)){
            return "";
        }
        StringBuilder warehouseNameListVal = new StringBuilder("(");
        for (Object obj : list) {
            warehouseNameListVal.append(QueryUtils.handleVal(obj, dataTypeEnum.getCode(), QueryConditionEnum.IN_LIST)).append(",");
        }
        //去掉最后一个,
        warehouseNameListVal = new StringBuilder(warehouseNameListVal.substring(0, warehouseNameListVal.length() - 1));
        warehouseNameListVal.append(")");
        return warehouseNameListVal.toString();
    }


    public static List<String> parseValueToStrList(Object value, QueryConditionEnum queryConditionEnum) {
        if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.NE.equals(queryConditionEnum)){
            if(null != value){
                return Collections.singletonList(value.toString());
            }
        }
        if (QueryConditionEnum.IN_LIST.equals(queryConditionEnum) || QueryConditionEnum.NOT_IN_LIST.equals(queryConditionEnum)){
            if(null != value) {
                return JSONArray.parseArray(JSON.toJSONString(value))
                        .stream()
                        .map(Object::toString)
                        .distinct()
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
