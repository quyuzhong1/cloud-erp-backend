package com.common.business.aspect;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.IQueryHandler;
import com.common.business.utils.QueryUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class WebAdvanceQueryAspect {

    public static final String ADVANCE_QUERY_FIELD_NAME = "advanceQueryDTOList";

    public static final String SQL_MAP_FIELD_NAME = "sqlMap";

    private static Logger logger = LoggerFactory.getLogger(WebAdvanceQueryAspect.class);

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
        IQueryHandler queryHandler;
        //扩展处理类和扩展字段
        if(controllerDataScope.handler() != IQueryHandler.class) {
            queryHandler = context.getBean(controllerDataScope.handler());
        }else{
            queryHandler = null;
        }

        //获取查询对象(如果为空会初始化一个长度为1的集合）
        List<AdvanceQueryDTO> advanceQueryDTOList = this.getQueryDTOList(point);

        if(Objects.isNull(advanceQueryDTOList)){
            throw new ServiceException("获取不到高级查询对象，请确认前端传参和后端参数");
        }
        //group 为空默认为default
        advanceQueryDTOList.replaceAll(v -> {
            v.setGroupName(StringUtils.defaultIfBlank(v.getGroupName(), "default"));
            return v;
        });
        Map<String, List<AdvanceQueryDTO>> advanceQueryDTOMap = advanceQueryDTOList.stream().collect(Collectors.groupingBy(AdvanceQueryDTO::getGroupName));
        advanceQueryDTOMap.forEach((key,val)->{
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("1 = 1 ");
            if(val.size() > 1 || (StringUtils.isNotBlank(val.get(0).getField()) &&  StringUtils.isNotBlank(val.get(0).getCompare()))){
                stringBuilder.append(" and ");
            }
            val .get(val.size() - 1).setCompareSymbol("");
            for (AdvanceQueryDTO dto : val) {
                if (Objects.isNull(dto.getField()) || Objects.isNull(dto.getCompare())) {
                    continue;
                }
                QueryConditionEnum condEnum = QueryConditionEnum.CODE_MAPS.get(dto.getCompare());
                //校验字段跟连接符合法性，防SQL注入
                if (!SqlUtils.verifySqlLegality(dto.getField()) || (Objects.nonNull(dto.getValue()) && !SqlUtils.verifySqlLegality(dto.getValue().toString()))) {
                    throw new ServiceException(ApiError.QUERY_ILLEGAL_FIELD);
                }
                if (condEnum == null) {
                    throw new ServiceException(ApiError.QUERY_ILLEGAL_COND);
                }
                if(!QueryConditionEnum.SET_NO_VAL.contains(condEnum) && (ObjectUtil.isEmpty(dto.getValue()) || (CharSequenceUtil.isEmpty(dto.getValue().toString())))){
                    stringBuilder.append(" 1 = 1 ").append(dto.getCompareSymbol()).append(" ");
                    continue;
                }
                stringBuilder.append(this.splicingSQL(dto,queryHandler));
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

    @AfterThrowing(pointcut = ("pointCut()"), throwing = "exception")
    public void logExceptionAndParameters(JoinPoint point, Exception exception) throws IllegalAccessException {
        // 获取方法参数
        WebAdvanceQuery controllerDataScope = this.getAnnotationLog(point);
        if (controllerDataScope == null) {
            return;
        }
        Map<String,String> sqlMap = this.getSqlMap(point);
        logger.error("方法：{}异常，sqlMap:{}",point.getSignature(),sqlMap);
    }

    /**
     * 拼接字符串 顺序: 左括号+字段+比较符+属性值+ 连接符+右括号
     *
     * @param dto
     * @param
     * @return
     */
    private String splicingSQL(AdvanceQueryDTO dto,IQueryHandler queryHandler){
        QueryConditionEnum condEnum = QueryConditionEnum.CODE_MAPS.get(dto.getCompare());
        StringBuilder sql = new StringBuilder();
        for(int i = 0; i<dto.getLeftBracketCount();i++){
            sql.append("(");
        }
        String contentSql;
        if(Objects.nonNull(dto.getIsExtend())&&dto.getIsExtend()){
            if(queryHandler == null){
                throw new ServiceException(ApiError.QUERY_NOT_EXTEND_CLASS);
            }
            String compareValueSQL = QueryUtils.splicingCompareValueSQL(condEnum,dto);
            contentSql = queryHandler.splicingSQL(dto.getField(),condEnum.getCode(),dto.getValue(),compareValueSQL);
            if(StringUtils.isBlank(contentSql)){
                throw new ServiceException(ApiError.QUERY_NOT_EXTEND_METHOD);
            }
            if(QueryConditionEnum.STARTS_WITH.equals(condEnum) || QueryConditionEnum.ENDS_WITH.equals(condEnum) || QueryConditionEnum.CONTAINS.equals(condEnum)) {
            	String[] likeSplit = contentSql.split("like LOWER");
            	if(likeSplit.length == 2) {
            		String[] blankSplit = likeSplit[0].split(" ");
                	String likeQuery = blankSplit[blankSplit.length - 1];
                	if(!likeQuery.toUpperCase().contains("LOWER")) {
                		if(contentSql.split(likeQuery).length == 2) {
                			contentSql = contentSql.replace(likeQuery, " LOWER(" + likeQuery + ") ");
                		}
                	}
            	}
            }
        }else{

            //为空处理为  (字段 = '' or 字段 is null)，不为空处理为  字段 != '' 其他直接拼接
            if(QueryConditionEnum.IS_NULL.equals(condEnum)){
                sql.append("(" + dto.getField() +" = '' or "+dto.getField()+" is null)").append(" ");
            } else if (QueryConditionEnum.NOT_NULL.equals(condEnum)) {
                sql.append( dto.getField() +" != ''").append(" ");
            } else if (QueryConditionEnum.STARTS_WITH.equals(condEnum) || QueryConditionEnum.ENDS_WITH.equals(condEnum) || QueryConditionEnum.CONTAINS.equals(condEnum)) {
                sql.append("LOWER(").append(dto.getField()).append(")").append(" ");
            } else if (QueryConditionEnum.NOT_CONTAINS.equals(condEnum)) {
                sql.append("LOWER(").append(dto.getField()).append(")").append(" ");
            } else{
                sql.append(dto.getField()).append(" ");
            }
            contentSql = QueryUtils.splicingCompareValueSQL(condEnum,dto);
        }
        sql.append(contentSql).append(" ");

        sql.append(dto.getCompareSymbol()).append(" ");

        for(int i = 0; i<dto.getRightBracketCount();i++){
            sql.append(")");
        }
        return sql.toString();
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
        if (resultClz == null) {
            return null;
        }
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
        return getMapWithFieldName(resultClz.getSuperclass(), arg);
    }

    private List<AdvanceQueryDTO> getListWithFieldName(Class<?> resultClz, Object arg) throws IllegalAccessException {
        if (resultClz == null) {
            return null;
        }
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
                advanceQueryDTO.setGroupName("default");
                newValue.add(advanceQueryDTO);
                field.set(arg, newValue);
                return (List<AdvanceQueryDTO>) field.get(arg);
            }
            return (List<AdvanceQueryDTO>) fieldValue;
        }
        return getListWithFieldName(resultClz.getSuperclass(), arg);
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

}
