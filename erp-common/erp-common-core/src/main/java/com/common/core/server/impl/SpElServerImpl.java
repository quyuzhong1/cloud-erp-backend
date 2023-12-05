package com.common.core.server.impl;


import cn.hutool.json.JSONObject;
import com.common.core.dto.SpElAddFieldDTO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.server.rule.SpElServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * @param conditionList
     * @param obj
     * @return
     */
    @Override
    public Boolean matchExpressionByConditionList(List<ConditionElement> conditionList, JSONObject obj) {
        for (String key : obj.keySet()) {
            String value = obj.get(key).toString();
            obj.set(key, value);
        }
        List<JSONObject> jsonList=obj.getBeanList("detailList",JSONObject.class);
        SpElExpressionDTO spElDTO = getConditionExpression(conditionList, obj);
        List<SpElAddFieldDTO> addFieldList = spElDTO.getSpElAddFieldList();
        for (SpElAddFieldDTO item : addFieldList) {
            //原始字段
            String originalField = item.getOriginalField();
            List<String> valueList = getValueList(originalField, jsonList);
            String addField = item.getNeedAddField();
            obj.set(addField,valueList);
        }


        return matchExpression(spElDTO.getExpression(), obj);

    }

    /**
     * 获取对应字段的值
     *
     * @param originalField
     * @param jsonList
     * @return
     */
    private List<String> getValueList(String originalField, List<JSONObject> jsonList) {
        List<String> list = new ArrayList<>(jsonList.size());
        for (JSONObject obj : jsonList) {
            String value = obj.getOrDefault(originalField, "").toString();
            if (StringUtils.isNotBlank(value)) {
                list.add(value);
            }
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
            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(compare)) {
                String content = new StringBuilder("['").append(field).append("'] ").append(compare).append(" '").append(value).append("'").toString();
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(compare);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
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
                        case IS_NULL:
                            content = convertToIsNullMapExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullMapExpression(field);
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
            String listFlag = "List";
            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(compare)) {
                String content = new StringBuilder().append(field).append(" ").append(compare).append(" '").append(value).append("'").toString();
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(compare);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
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
                        case IS_NULL:
                            content = convertToIsNullObjExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullObjExpression(field);
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
        return content.replace(" contains ", ".contains(") + ")";
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
}
