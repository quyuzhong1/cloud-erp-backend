package com.common.business.aspect;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.IQueryHandler;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.SqlUtils;
import com.common.core.utils.date.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class WebAdvanceQueryAspect {

    public static final String ADVANCE_QUERY_FIELD_NAME = "advanceQueryList";

    public static final String SQL_MAP_FIELD_NAME = "sqlMap";

    @Resource
    private ApplicationContext context;

    // 配置织入点
    @Pointcut("@annotation(com.common.business.annotation.WebAdvanceQuery)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        //获取注解
        WebAdvanceQuery controllerDataScope = this.getAnnotationLog(point);
        if (controllerDataScope == null) {
            return;
        }
        //扩展处理类和扩展字段
        IQueryHandler queryHandler = context.getBean(controllerDataScope.handler());
        String[] extendFieldArr = controllerDataScope.extendFieldArr();
        List<String> extendFieldList = Arrays.asList(extendFieldArr);

        //获取查询对象(如果为空会初始化一个长度为1的集合）
        List<AdvanceQueryDTO> advanceQueryDTOList = this.getQueryDTOList(point);

        //group 为空默认为default
        advanceQueryDTOList.replaceAll(v -> {
            v.setGroup(StringUtils.defaultIfBlank(v.getGroup(), "default"));
            return v;
        });
        Map<String, List<AdvanceQueryDTO>> advanceQueryDTOMap = advanceQueryDTOList.stream().collect(Collectors.groupingBy(AdvanceQueryDTO::getGroup));
        advanceQueryDTOMap.forEach((key,val)->{
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("1 = 1 ");
            if(val.size() > 1 || StringUtils.isNotBlank(val.get(0).getField()) ||  StringUtils.isNotBlank(val.get(0).getCompare())){
                stringBuilder.append(" and ");
            }
            val .get(val.size() - 1).setCompareSymbol("");
            for (AdvanceQueryDTO dto : val) {
                if (Objects.isNull(dto.getValue()) || Objects.isNull(dto.getCompare())) {
                    continue;
                }
                //校验字段跟连接符合法性，防SQL注入
                if (!SqlUtils.verifySqlLegality(dto.getField()) || (Objects.nonNull(dto.getValue()) && !SqlUtils.verifySqlLegality(dto.getValue().toString()))) {
                    throw new ServiceException(ApiError.QUERY_ILLEGAL_FIELD);
                }
                if (!QueryConditionEnum.CODE_MAPS.containsKey(dto.getCompare())) {
                    throw new ServiceException(ApiError.QUERY_ILLEGAL_COND);
                }
                stringBuilder.append(this.splicingSQL(dto,queryHandler,extendFieldList.contains(dto.getField())));
            }
            //将sql设置到sqlMap中
            Map<String,String> sqlMap;
            try {
                sqlMap = this.getSqlMap(point);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            sqlMap.put(key,stringBuilder.toString());
        });
    }

    /**
     * 拼接字符串 顺序: 左括号+字段+比较符+属性值+ 连接符+右括号
     *
     * @param dto
     * @param
     * @return
     */
    private String splicingSQL(AdvanceQueryDTO dto,IQueryHandler queryHandler,boolean isExtend){
        QueryConditionEnum condEnum = QueryConditionEnum.CODE_MAPS.get(dto.getCompare());
        StringBuilder sql = new StringBuilder();
        for(int i = 0; i<dto.getLeftBracketCount();i++){
            sql.append("(");
        }
        String contentSql;
        if(isExtend){
            String val = this.handleVal(dto.getValue(),dto.getDataType(),condEnum);
            String compareValueSQL = this.splicingCompareValueSQL(condEnum,dto);
            contentSql = queryHandler.splicingSQL(dto.getField(),condEnum.getCode(),val,compareValueSQL);
            if(StringUtils.isBlank(contentSql)){
                throw new ServiceException("扩展字段没有配置查询脚本");
            }
        }else{

            //为空处理为  (TRIM(both ' ' FROM 字段) = ''or 字段 is null)，不为空处理为  TRIM(both ' ' FROM 字段) != '' 其他直接拼接
            if(QueryConditionEnum.IS_NULL.equals(condEnum)){
                sql.append("(TRIM(both ' ' FROM " + dto.getField() +") = ''or "+dto.getField()+" is null)").append(" ");
            } else if (QueryConditionEnum.NOT_NULL.equals(condEnum)) {
                sql.append("TRIM(both ' ' FROM "+ dto.getField() +") != ''").append(" ");
            } else{
                sql.append(dto.getField()).append(" ");
            }
            contentSql = this.splicingCompareValueSQL(condEnum,dto);
        }
        sql.append(contentSql).append(" ");

        sql.append(dto.getCompareSymbol()).append(" ");

        for(int i = 0; i<dto.getRightBracketCount();i++){
            sql.append(")");
        }
        return sql.toString();
    }

    private String splicingCompareValueSQL(QueryConditionEnum condEnum,AdvanceQueryDTO dto){
        StringBuilder sql = new StringBuilder();
        //starts_with 和 ends_with 处理成like，为空和不为空和between不处理
        if(QueryConditionEnum.STARTS_WITH.equals(condEnum) || QueryConditionEnum.ENDS_WITH.equals(condEnum)){
            sql.append(QueryConditionEnum.CONTAINS.getCode()).append(" ");
        } else if (!QueryConditionEnum.IS_NULL.equals(condEnum) && !QueryConditionEnum.NOT_NULL.equals(condEnum) &&  !QueryConditionEnum.BETWEEN.equals(condEnum)){
            sql.append(condEnum.getCode()).append(" ");
        }
        String val = "";
        //大于小于等于这种直接拼接值
        if(QueryConditionEnum.SET_DIRECT_VAL.contains(condEnum)){
            val = this.handleVal(dto.getValue(),dto.getDataType(),condEnum);
        }
        //in 查询拼接成(val1,val2)格式
        if(QueryConditionEnum.SET_IN.contains(condEnum)){
            if(dto.getValue().getClass() != ArrayList.class){
                throw new ServiceException(ApiError.QUERY_LIST_TYPE_ERROR);
            }
            ArrayList<Object> list = (ArrayList<Object>) dto.getValue();
            val = "(" + val;
            for(Object obj : list){
                val = val + this.handleVal(obj,dto.getDataType(),condEnum) + ",";
            }
            //去掉最后一个,
            val = val.substring(0, val.length() - 1);
            val = val+")";
        }

        //like查询拼接成 concat('%',#{val},'%') 或concat(#{val},'%') 或concat('%',#{val})
        if(QueryConditionEnum.SET_LIKE.contains(condEnum)){
            if(QueryConditionEnum.STARTS_WITH.equals(condEnum)){
                val = "'" + dto.getValue() + "%'";
            }else if(QueryConditionEnum.ENDS_WITH.equals(condEnum)){
                val = "'%" + dto.getValue() + "'";
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
                throw new ServiceException("介于条件需要填起始时间和开始时间");
            }
            String startDate = list.get(0).toString();
            String endDate = list.get(1).toString();
            if(DateUtil.isDateOrTimeValid(list.get(0).toString()) && DateUtil.isDateOrTimeValid(list.get(1).toString())){
                String interval = this.getDateStr(endDate);
                startDate = "'"+startDate+"'";
                endDate = "'"+endDate+"'";
                if(StringUtils.isNotBlank(interval)){
                    val = " >= to_timestamp("+startDate+",'yyyy-MM-DD HH24:MI:SS')  and " + dto.getField()+" < (to_timestamp("+endDate+",'yyyy-MM-DD HH24:MI:SS')::TIMESTAMP + INTERVAL '1"+ interval+"')  ";
                }else{
                    val = " >= to_timestamp("+startDate+",'yyyy-MM-DD HH24:MI:SS')  and " + dto.getField()+" < to_timestamp("+endDate+",'yyyy-MM-DD HH24:MI:SS') ";
                }
            }else{
                throw new ServiceException("非法日期格式");
            }
        }
        sql.append(val);
        return sql.toString();
    }

    private String handleVal(Object fieldVal,String dataType,QueryConditionEnum condEnum) {
        String result = fieldVal.toString();
        QueryDataTypeEnum queryDataTypeEnum = EnumMessage.getByCode(QueryDataTypeEnum.class,dataType);
        if(queryDataTypeEnum.equals(QueryDataTypeEnum.STRING)){
            result = "'"+ fieldVal + "'";
        }else if (queryDataTypeEnum.equals(QueryDataTypeEnum.DATE)){
            if(DateUtil.isDateOrTimeValid(fieldVal.toString())){
                String interval = this.getDateStr(result);
                result = "'"+ fieldVal + "'";
                if(condEnum.equals(QueryConditionEnum.LE) && StringUtils.isNotBlank(interval)){
                    result = "to_timestamp("+ result + ",'yyyy-MM-DD HH24:MI:SS')::TIMESTAMP + INTERVAL '1 "+interval+"' ";
                }else{
                    result = "to_timestamp(" + result + ",'yyyy-MM-DD HH24:MI:SS')";
                }
            }else{
                throw new ServiceException("非法日期格式");
            }
        }
        return result;
    }


    private Map<String,String> getSqlMap(final JoinPoint point) throws IllegalAccessException {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            Class<?> resultClz = arg.getClass();
            //如果是导出的话，会在这里返回
            Map<String,String> map = this.getMapWithFieldName(resultClz,arg);
            if(Objects.nonNull(map)){
                return map;
            }
            Field[] fieldInfo = resultClz.getDeclaredFields();
            for (Field field : fieldInfo) {
                field.setAccessible(true);
                Object fieldValue = field.get(arg);
                if(fieldValue == null){
                    continue;
                }
                map = this.getMapWithFieldName(fieldValue.getClass(),fieldValue);
                if(Objects.nonNull(map)){
                    return map;
                }
            }
        }
        return null;
    }


    private List<AdvanceQueryDTO> getQueryDTOList(final JoinPoint point) throws IllegalAccessException {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            Class<?> resultClz = arg.getClass();
            //如果是导出的话，会在这里返回
            List<AdvanceQueryDTO> list = this.getListWithFieldName(resultClz,arg);
            if(Objects.nonNull(list)){
                return list;
            }
            Field[] fieldInfo = resultClz.getDeclaredFields();
            for (Field field : fieldInfo) {
                field.setAccessible(true);
                Object fieldValue = field.get(arg);
                if(fieldValue == null){
                    continue;
                }
                list = this.getListWithFieldName(fieldValue.getClass(),fieldValue);
                if(Objects.nonNull(list)){
                    return list;
                }
            }
        }
        return null;
    }

    private Map<String,String> getMapWithFieldName(Class<?> resultClz, Object arg) throws IllegalAccessException {
        Field[] fieldInfo = resultClz.getDeclaredFields();
        for (Field field : fieldInfo) {
            if (!SQL_MAP_FIELD_NAME.equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Object fieldValue = field.get(arg);
            if (fieldValue == null) {
                Map<String,String> newValue = new HashMap<>();
                field.set(arg, newValue);
                return (Map<String,String>) field.get(arg);
            }
            return ( Map<String,String>) fieldValue;
        }
        return null;
    }

    private List<AdvanceQueryDTO> getListWithFieldName(Class<?> resultClz, Object arg) throws IllegalAccessException {
        Field[] fieldInfo = resultClz.getDeclaredFields();
        for (Field field : fieldInfo) {
            if (!ADVANCE_QUERY_FIELD_NAME.equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Object fieldValue = field.get(arg);
            if (fieldValue == null || ((List<?>) fieldValue).isEmpty()) {
                List<AdvanceQueryDTO> newValue = new ArrayList<>();
                AdvanceQueryDTO advanceQueryDTO = new AdvanceQueryDTO();
                advanceQueryDTO.setGroup("default");
                newValue.add(advanceQueryDTO);
                field.set(arg, newValue);
                return (List<AdvanceQueryDTO>) field.get(arg);
            }
            return (List<AdvanceQueryDTO>) fieldValue;
        }
        return null;
    }

    /**
     * 获取注解，不存在返回null
     */
    private WebAdvanceQuery getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(WebAdvanceQuery.class);
        }
        return null;
    }

    private String getDateStr(String input) {
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
}
