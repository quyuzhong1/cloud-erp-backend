package com.common.core.server.impl;


import com.common.core.dto.SpElAddFieldDTO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.server.rule.SpElServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-07 10:35
 */
@Service
@Slf4j
public class SpElServerImpl implements SpElServer {

    /**
     * 获取到条件表达式
     *
     * @param conditionElementList
     * @return
     */
    @Override
    public SpElExpressionDTO getConditionExpression(List<ConditionElement> conditionElementList, Object obj) {
        if (Objects.isNull(obj) || obj instanceof Map) {
            return getConditionExpressionByMap(conditionElementList);
        } else {
            return getConditionExpressionByObj(conditionElementList);
        }

    }


    /**
     * 检查表达式是否正确
     *
     * @param expression
     * @return
     */
    @Override
    public Boolean checkExpressionIsEnabled(String expression) {
        if (StringUtils.isBlank(expression)) {
            return Boolean.FALSE;
        }
        ExpressionParser parser = new SpelExpressionParser();
        try {
            parser.parseExpression(expression);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("{} 表达式出错>>>>>>", expression);
            return Boolean.FALSE;
        }
    }

    /**
     * 匹配表达式结果
     *
     * @param expressionStr
     * @param obj
     * @return
     */
    @Override
    public Boolean matchExpression(String expressionStr, Object obj) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(expressionStr);
            EvaluationContext context = new StandardEvaluationContext(obj);
            Boolean result = expression.getValue(context, Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("匹配spEl 表达式有误{}", e);
        }
        return Boolean.FALSE;
    }

    /**
     * 匹配表达式结果
     *
     * @param spElDTO
     * @param obj
     * @return
     */
    @Override
    public Boolean matchExpressionWithVariable(SpElExpressionDTO spElDTO, Object obj) {
        String expressionStr = spElDTO.getExpression();
        Map<String, Object> variables = spElDTO.getVariables();
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(expressionStr);
            log.info("校验规则：{}", expressionStr);
            EvaluationContext context = new StandardEvaluationContext(obj);
            if (!variables.isEmpty()){
                for (String key : variables.keySet()){
                    Object value = variables.get(key);
                    context.setVariable(key, value);
                }
            }
            Boolean result = expression.getValue(context, Boolean.class);
            log.info("校验结果：{}",result);
            return result;
        } catch (Exception e) {
            log.error("匹配spEl 表达式有误{}", e);
        }
        return Boolean.FALSE;
    }


    /**
     * 匹配表达式结果
     *
     * @param conditionList
     * @param obj
     * @return
     */
    @Override
    public Boolean matchExpressionByConditionList(List<ConditionElement> conditionList, Map<String, Object> obj) {
        SpElExpressionDTO spElDTO = conditionExpressionByMap(conditionList, obj);
        List<SpElAddFieldDTO> addFieldList = spElDTO.getSpElAddFieldList();
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) obj.get("detailList");
        for (SpElAddFieldDTO item : addFieldList) {
            //原始字段
            String originalField = item.getOriginalField();
            List<Object> valueList = getValueList(originalField, mapList);
            String addField = item.getNeedAddField();
            obj.put(addField, valueList);
        }
        return matchExpressionWithVariable(spElDTO, obj);

    }

    @Override
    public Boolean matchDetailExpressionByConditionList(List<ConditionElement> conditionList, Map<String, Object> obj) {
        SpElExpressionDTO spElDTO = conditionExpressionByMap(conditionList, obj);
        List<SpElAddFieldDTO> addFieldList = spElDTO.getSpElAddFieldList();
        for (SpElAddFieldDTO item : addFieldList) {
            //原始字段
            String originalField = item.getOriginalField();
            Object value = obj.get(originalField);
            String addField = item.getNeedAddField();
            obj.put(addField, value);
        }
        return matchExpressionWithVariable(spElDTO, obj);

    }

    @Override
    public  Object getByField(String fieldCode, List<Map<String, Object>> mapList) {
        Set<Object> set = new HashSet<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            Object obj = map.getOrDefault(fieldCode, "");
            if (Objects.nonNull(obj)) {
                set.add(obj);
            }
        }
        if (CollectionUtils.isEmpty(set)){
            return null;
        }else if (set.size() == 1){
            return set.stream().findFirst().get();
        }else {
            StringBuilder sb = new StringBuilder();
            for (Object obj : set){
                if (sb.length() > 0){
                    sb.append(",");
                }
                sb.append(obj.toString());
            }
            return sb.toString();
        }

    }

    /**
     * 获取对应字段的值
     *
     * @param originalField
     * @param mapList
     * @return
     */
    private List<Object> getValueList(String originalField, List<Map<String, Object>> mapList) {
        List<Object> list = new ArrayList<>(mapList.size());
        for (Map<String, Object> obj : mapList) {
            Object value = obj.get(originalField);
            list.add(value);
        }
        return list;
    }

    /**
     * 获取到 传值为map 的 表达式
     *
     * @param conditionElementList
     * @return
     */
    private SpElExpressionDTO getConditionExpressionByMap(List<ConditionElement> conditionElementList) {
        SpElExpressionDTO spElDTO = new SpElExpressionDTO();
        //需要加的字段
        List<SpElAddFieldDTO> addFieldList = new ArrayList<>(5);

        StringBuilder expression = new StringBuilder();
        for (ConditionElement element : conditionElementList) {

            //左括号
            String leftBracket = element.getLeftBracket();
            if (StringUtils.isNotBlank(leftBracket)) {
                expression.append(leftBracket).append(" ");
            }
            //字段
            String field = element.getField();

            //关系 大于 等于之类
            String compare = element.getCompare();

            //对应的值
            String value = element.getValue();
            //值的类型
            String valueType = element.getValueType();
            Object conversionValue = conversionValue(value, valueType);
            Boolean isStr = "String".equals(valueType);

            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(compare)) {
                String content = getContent(field,compare,conversionValue,isStr, new HashMap<>());
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(compare);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
                        case CONTAINS:
                            String addField = getAddField(field, addFieldList);
                            content = getContentList(field,addField,compare,conversionValue,spElDTO, new HashMap<>());
                            break;
                        case NOT_CONTAINS:
                            String addField1 = getAddField(field, addFieldList);
                            content = getContentList(field,addField1,compare,conversionValue,spElDTO, new HashMap<>());
                            break;
                        case IN_LIST:
                            String addField2 = getAddField(field, addFieldList);
                            content = getContentList(field,addField2,compare,value,spElDTO, new HashMap<>());
                            break;
                        case NOT_IN_LIST:
                            String addField3 = getAddField(field, addFieldList);
                            content = getContentList(field,addField3,compare,value,spElDTO, new HashMap<>());
                            break;
                        case IS_NULL:
                            content = convertToIsNullMapExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullMapExpression(field);
                            break;
                        case STARTS_WITH:
                            content = convertToStartsWithObjExpression(field,value);
                            break;
                    }
                }
                expression.append(content).append(" ");
            }
            //右括号
            String rightBracket = element.getRightBracket();
            if (StringUtils.isNotBlank(rightBracket)) {
                expression.append(rightBracket).append(" ");
            }
            //逻辑关系
            String logic = element.getLogic();
            if (StringUtils.isNotBlank(logic)) {
                expression.append(logic).append(" ");
            }
        }
        spElDTO.setExpression(expression.toString());
        spElDTO.setSpElAddFieldList(addFieldList);
        return spElDTO;
    }

    /**
     * 列表规则支持  增加表达式传递
     * @param field
     * @param addField
     * @param compare
     * @param targetValue
     * @param spElDTO
     * @param detailMap
     * @return
     */
    private String getContentList(String field, String addField, String compare, Object targetValue, SpElExpressionDTO spElDTO, Map<String, Object> detailMap) {
        List<Object> targetValueStr = Arrays.asList(targetValue.toString().split(","));
        StringBuilder sb=new StringBuilder();
        //要对比的值  变量.contains
        Object o = detailMap.get(field);
        String fieldStr;
        if (Objects.nonNull(o)){
            fieldStr = o.toString();
        }else {
            fieldStr = "";
        }
        List<String> split1 = Arrays.stream(fieldStr.split(",")).collect(Collectors.toList());
        boolean startsWith = fieldStr.startsWith(",");
        boolean endsWith = fieldStr.endsWith(",");
        boolean contains = fieldStr.contains(",,");
        if (startsWith || endsWith || contains){
            if (CollectionUtils.isEmpty(split1)){
                split1 = new ArrayList<>();
            }
            split1.add("");
        }

        if (split1.size() > 1){
            sb.append("(");
            for (int i = 0; i < split1.size(); i++) {
                if (RuleCompareEnum.CONTAINS.getCode().equals(compare) || RuleCompareEnum.IN_LIST.getCode().equals(compare)){
                    sb.append("#").append(addField).append(".contains");
                }else if (RuleCompareEnum.NOT_CONTAINS.getCode().equals(compare) || RuleCompareEnum.NOT_IN_LIST.getCode().equals(compare)){
                    sb.append("!#").append(addField).append(".contains");
                }
                sb.append("('").append(split1.get(i)).append("')");
                if (i + 1 < split1.size()){
                    if (RuleCompareEnum.CONTAINS.getCode().equals(compare) || RuleCompareEnum.NOT_CONTAINS.getCode().equals(compare)){
                        sb.append(" or ");
                    }else if (RuleCompareEnum.IN_LIST.getCode().equals(compare) || RuleCompareEnum.NOT_IN_LIST.getCode().equals(compare)){
                        sb.append(" and ");
                    }
                }
            }
            sb.append(")");
        }else {
            if (RuleCompareEnum.CONTAINS.getCode().equals(compare) || RuleCompareEnum.IN_LIST.getCode().equals(compare)){
                sb.append("#").append(addField).append(".contains");
            }else if (RuleCompareEnum.NOT_CONTAINS.getCode().equals(compare) || RuleCompareEnum.NOT_IN_LIST.getCode().equals(compare)){
                sb.append("!#").append(addField).append(".contains");
            }
            sb.append("('").append(fieldStr).append("')");
        }
        Map<String, Object> variables = spElDTO.getVariables();
        //是否存在记录
        Object object = variables.get(addField);
        if (Objects.nonNull(object) && object instanceof List){
            //列表
            List<Object> list = (List<Object>)object;
            //对于非数组 就传递字符串
            if (targetValueStr.size() > 1){
                List<Object> list3 = Stream.of(list, targetValueStr).flatMap(List::stream).collect(Collectors.toList());
                variables.put(addField, list3);
            }else {
                list.add(targetValue);
                variables.put(addField, list);
            }
        }else {
            List<Object> list = new ArrayList<>();
            //对于非数组 就传递字符串
            if (targetValueStr.size() > 1){
                if (Objects.nonNull(object)){
                    targetValueStr.add(object);
                }
                variables.put(addField, targetValueStr);
            }else {
                if (Objects.nonNull(object)){
                    list.add(object);
                }
                list.add(targetValue);
                variables.put(addField, list);
            }
        }

        spElDTO.setVariables(variables);
        return sb.toString();
    }

    /**
     * 获取contemt的值
     *
     * @param field       字段
     * @param compare     比较符号
     * @param targetValue 目标值
     * @param isStr       是否是String
     * @param obj
     * @return
     * @author yl
     * @date 2023-12-06 15:27
     */

    private String getContent(String field, String compare, Object targetValue, Boolean isStr, Map<String, Object> obj) {
        StringBuilder sb=new StringBuilder();
        //需要适配(订单-明细)级别比较 x-(a,b)
        Object filedValueObj = obj.get(field);
        //不为空，且是string类型 且字段存在明细汇总到订单中
        if (Objects.nonNull(filedValueObj) && filedValueObj instanceof String && ((String) filedValueObj).contains(",")){
            String[] split = filedValueObj.toString().split(",");
            sb.append("(");
            for (int i = 0; i < split.length; i++) {

                if(isStr){
                    sb.append("'").append(split[i]).append("'");
                    sb.append(" ").append(compare).append(" ");
                    sb.append("'").append(targetValue).append("'");
                }else{
                    sb.append(split[i]);
                    sb.append(" ").append(compare).append(" ");
                    sb.append(targetValue);
                }
                if (i + 1 < split.length){
                    sb.append(" and ");
                }
            }
            sb.append(")");
        }else {
            sb.append("['");
            sb.append(field).append("'] ");
            sb.append(compare);
            sb.append(" ");
            if(isStr){
                sb.append("'");
                sb.append(targetValue);
                sb.append("'");
            }else{
                sb.append(targetValue);
            }
        }

        return sb.toString();
    }

    /**
     * 转化值
     *
     * @param value
     * @param valueType
     * @return
     */
    private Object conversionValue(String value, String valueType) {
        if ("String".equals(valueType)) {
            return value;
        }

        if ("BigDecimal".equals(valueType)) {
            if(StringUtils.isBlank(value)){
               return "null";
            }
            return new BigDecimal(value);
        }
        return value;
    }

    private String getAddField(String field, List<SpElAddFieldDTO> addFieldList) {
        SpElAddFieldDTO addFieldDTO = new SpElAddFieldDTO();
        String addField = field + "List";
        addFieldDTO.setNeedAddField(addField);
        addFieldDTO.setOriginalField(field);
        addFieldList.add(addFieldDTO);
        return addField;
    }


    /**
     * 获取到 传值为对象的表达式
     *
     * @param conditionElementList
     * @return
     */
    private SpElExpressionDTO getConditionExpressionByObj(List<ConditionElement> conditionElementList) {
        SpElExpressionDTO spElDTO = new SpElExpressionDTO();
        StringBuilder expression = new StringBuilder();
        //需要加的字段
        List<SpElAddFieldDTO> addFieldList = new ArrayList<>(5);

        for (ConditionElement element : conditionElementList) {
            //左括号
            String leftBracket = element.getLeftBracket();
            if (StringUtils.isNotBlank(leftBracket)) {
                expression.append(leftBracket).append(" ");
            }
            //字段
            String field = element.getField();

            //关系 大于 等于之类
            String compare = element.getCompare();

            //对应的值
            String value = element.getValue();

            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(compare)) {
                String content = new StringBuilder().append(field).append(" ").append(compare).append(" '").append(value).append("'").toString();
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(compare);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
                        case CONTAINS:
                            String addField = getAddField(field, addFieldList);
                            content = getContentList(field,addField,compare,value,spElDTO, new HashMap<>());
                            break;
                        case NOT_CONTAINS:
                            String addField1 = getAddField(field, addFieldList);
                            content = getContentList(field,addField1,compare,value,spElDTO, new HashMap<>());
                            break;
                        case IN_LIST:
                            String addField2 = getAddField(field, addFieldList);
                            content = getContentList(field,addField2,compare,value,spElDTO, new HashMap<>());
                            break;
                        case NOT_IN_LIST:
                            String addField3 = getAddField(field, addFieldList);
                            content = getContentList(field,addField3,compare,value,spElDTO, new HashMap<>());
                            break;
                        case IS_NULL:
                            content = convertToIsNullObjExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullObjExpression(field);
                            break;
                        case STARTS_WITH:
                            content = convertToStartsWithObjExpression(field,value);
                            break;
                    }
                }
                expression.append(content).append(" ");
            }
            //右括号
            String rightBracket = element.getRightBracket();
            if (StringUtils.isNotBlank(rightBracket)) {
                expression.append(rightBracket).append(" ");
            }
            //逻辑关系
            String logic = element.getLogic();
            if (StringUtils.isNotBlank(logic)) {
                expression.append(logic).append(" ");
            }
        }

        spElDTO.setExpression(expression.toString());
        spElDTO.setSpElAddFieldList(addFieldList);
        return spElDTO;
    }


    /**
     * 获取到转化成包含的
     *
     * @param content
     * @return
     */
    private String convertToContainsExpression(String content) {
        return content.replace("contains", ".contains(") + ")";
    }

    /**
     * 获取到转化成包含的
     *
     * @param content
     * @return
     */
    private String convertToNotContainsExpression(String content) {
        return "not " + content.replace(" notContains ", ".contains(") + ")";
    }

    /**
     * map对象表达式 转化为空
     *
     * @param field
     * @return
     */
    private String convertToIsNullMapExpression(String field) {
        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(" == null || ");
        expression.append("['").append(field).append("']");
        expression.append(" == ''");
        return expression.toString();
    }

    /**
     * 对象表达式 转化为空
     *
     * @param field
     * @return
     */
    private String convertToIsNullObjExpression(String field) {
        StringBuilder expression = new StringBuilder();
        expression.append("").append(field).append("");
        expression.append(" == null || ");
        expression.append("").append(field).append("");
        expression.append(" == ''");
        return expression.toString();
    }

    /**
     * map对象表达式 转化成不为空
     *
     * @param field
     * @return
     */
    private String convertToNotNullMapExpression(String field) {
//        StringBuilder sb=new StringBuilder();
//        sb.append("#").append(field);
//        sb.append(" != null and ");
//        sb.append("#").append(field);
//        sb.append(" == ''");
//        return sb.toString();

        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(" != null  && ");
        expression.append("['").append(field).append("']");
        expression.append(" != ''");
        return expression.toString();
    }

    /**
     * 对象表达式 转化成不为空
     *
     * @param field
     * @return
     */
    private String convertToNotNullObjExpression(String field) {
        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(" != null  && ");
        expression.append("['").append(field).append("']");
        expression.append(" != ''");
        return expression.toString();
    }

    /**
     *对象表达式 以...开头
     */
    private String convertToStartsWithObjExpression(String field,String value) {
        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(".startsWith");
        expression.append("(").append(value).append(")");
        return expression.toString();
    }

    /**
     * 获取到 传值为map 的 表达式
     *
     * @param conditionElementList
     * @param obj
     * @return
     */
    private SpElExpressionDTO conditionExpressionByMap(List<ConditionElement> conditionElementList, Map<String, Object> obj) {
        SpElExpressionDTO spElDTO = new SpElExpressionDTO();
        //需要加的字段
        List<SpElAddFieldDTO> addFieldList = new ArrayList<>(5);

        StringBuilder expression = new StringBuilder();
        for (ConditionElement element : conditionElementList) {

            //左括号
            String leftBracket = element.getLeftBracket();
            if (StringUtils.isNotBlank(leftBracket)) {
                expression.append(leftBracket).append(" ");
            }
            //字段
            String field = element.getField();

            //关系 大于 等于之类
            String compare = element.getCompare();

            //对应的值
            String value = element.getValue();
            //值的类型
            String valueType = element.getValueType();
            Object conversionValue = conversionValue(value, valueType);
            Boolean isStr = "String".equals(valueType);
            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(compare)) {
                String content = getContent(field,compare,conversionValue,isStr, obj);
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(compare);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
//                        case CONTAINS:
////                            String addField = getAddField(field, addFieldList);
////                            content = getContentList(field,addField,compare,conversionValue,spElDTO, obj);
//                            String addField = getAddField(field, addFieldList);
//                            content = getContent(addField,compare,conversionValue,isStr);
//                            content = convertToContainsExpression(content);
//                            break;
//                        case NOT_CONTAINS:
//                            String addField1 = getAddField(field, addFieldList);
//                            content = getContentList(field, addField1,compare,conversionValue,spElDTO, obj);
//                            break;
                        case CONTAINS:
                            String addField = getAddField(field, addFieldList);
                            content = new StringBuilder("['").append(addField).append("'] ").append(compare).append(" '").append(value).append("'").toString();
                            content = convertToContainsExpression(content);
                            break;
                        case NOT_CONTAINS:
                            String addField1 = getAddField(field, addFieldList);
                            content = new StringBuilder("['").append(addField1).append("'] ").append(compare).append(" '").append(value).append("'").toString();
                            content = convertToNotContainsExpression(content);
                            break;
                        case IN_LIST:
                            String addField2 = getAddField(field, addFieldList);
                            content = getContentList(field,addField2,compare,value,spElDTO, obj);
                            break;
                        case NOT_IN_LIST:
                            String addField3 = getAddField(field, addFieldList);
                            content = getContentList(field,addField3,compare,value,spElDTO, obj);
                            break;
                        case IS_NULL:
                            content = convertToIsNullMapExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullMapExpression(field);
                            break;
                        case STARTS_WITH:
                            content = convertToStartsWithObjExpression(field,value);
                            break;
                    }
                }
                expression.append(content).append(" ");
            }
            //右括号
            String rightBracket = element.getRightBracket();
            if (StringUtils.isNotBlank(rightBracket)) {
                expression.append(rightBracket).append(" ");
            }
            //逻辑关系
            String logic = element.getLogic();
            if (StringUtils.isNotBlank(logic)) {
                expression.append(logic).append(" ");
            }
        }
        spElDTO.setExpression(expression.toString());
        spElDTO.setSpElAddFieldList(addFieldList);
        return spElDTO;
    }
    public static void main(String[] args) {
        SpElServerImpl spElServer=new SpElServerImpl();
        ExpressionParser parser = new SpelExpressionParser();
        BigDecimal ss=new BigDecimal("2");
        String conditionExpression = "( ['packageWeight'].startsWith(pa) )";

        List<ConditionElement> conditionList=new ArrayList<>();
        ConditionElement conditionElement=new ConditionElement();
        conditionElement.setCompare("contains");
        conditionElement.setField("packageWeight");
        conditionElement.setLeftBracket("(");
        conditionElement.setLogic("");
        conditionElement.setRightBracket(")");
        conditionElement.setValue("2.00");
        conditionElement.setValueType("BigDecimal");
        conditionList.add(conditionElement);
        Map<String, Object> map = new HashMap<>();

        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> m1=new HashMap<>();
        m1.put("packageWeight",2.0);

        Map<String,Object> m2=new HashMap<>();
        m2.put("packageWeight",2.0);
        list.add(m1);
        list.add(m2);
        List<Double> list1=new ArrayList<>();
        list1.add(2.000000);
       // list1.add(new BigDecimal("2.100000"));
        map.put("packageWeightList",list1);
        Boolean result1=spElServer. matchExpression(conditionExpression, map);
        System.out.println(result1);


//        Boolean result= spElServer.matchExpressionByConditionList(conditionList,map);
//        System.out.println(result);
    }


}
