package com.common.core.rule;/**
 * @author Lambda
 * @Classname RuleUtils
 * @Description TODO
 * @Date 2023-09-04 11:07
 * @Created by yl
 */

import cn.hutool.json.JSONObject;
import com.common.core.enums.RuleCompareEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-04 11:07
 */
@Slf4j
public class SpELRuleUtils {

    /**
     * 获取到条件表达式
     *
     * @return
     */
    public static String getConditionExpression(List<ConditionElement> conditionElementList) {
        StringBuilder expression = new StringBuilder();
        for (ConditionElement element : conditionElementList) {
            //做括号
            String leftBracket = element.getLeftBracket();
            if (StringUtils.isNotBlank(leftBracket)) {
                expression.append(leftBracket).append(" ");
            }
            //字段
            String field = element.getField();
            //下拉的选项
            String operator = element.getCompare();
            //对应的值
            String value = element.getValue();
            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(operator)) {
                String content = new StringBuilder("['").append(field).append("'] ").append(operator).append(" '").append(value).append("'").toString();
                RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(operator);
                if (Objects.nonNull(contentsEnum)) {
                    switch (contentsEnum) {
                        case CONTAINS:
                            content = convertToContainsExpression(content);
                            break;
                        case NOT_CONTAINS:
                            content = convertToNotContainsExpression(content);
                            break;
                        case IS_NULL:
                            content = convertToIsNullExpression(field);
                            break;
                        case NOT_NULL:
                            content = convertToNotNullExpression(field);
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

        return expression.toString().trim();
    }


    /**
     * 获取到转化的表达式
     *
     * @param operator
     * @param content
     * @return
     */
    public static String getConvertExpression(String operator, String content) {
        RuleCompareEnum contentsEnum = RuleCompareEnum.getByCode(operator);
        if (Objects.isNull(contentsEnum)) {
            return content;
        }
        System.out.println(content);
        switch (contentsEnum) {
            case CONTAINS:
                content = convertToContainsExpression(content);
                return content;
            case NOT_CONTAINS:
                content = convertToNotContainsExpression(content);
                return content;
            case IS_NULL:
                content = convertToIsNullExpression(content);
                return content;
            default:
                return content;
        }

    }


    /**
     * 获取到转化成包含的
     *
     * @param content
     * @return
     */
    public static String convertToContainsExpression(String content) {
        return content.replace(" contains ", ".contains(") + ")";
    }

    /**
     * 获取到转化成包含的
     *
     * @param content
     * @return
     */
    public static String convertToNotContainsExpression(String content) {
        return "not " + content.replace(" notContains ", ".contains(") + ")";
    }

    /**
     * 转化为空
     *
     * @param field
     * @return
     */
    public static String convertToIsNullExpression(String field) {
        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(" eq null || ");
        expression.append("['").append(field).append("']");
        expression.append(" eq ''");
        return expression.toString();
    }

    /**
     * 转化成不为空
     *
     * @param field
     * @return
     */
    public static String convertToNotNullExpression(String field) {
        StringBuilder expression = new StringBuilder();
        expression.append("['").append(field).append("']");
        expression.append(" ne null  && ");
        expression.append("['").append(field).append("']");
        expression.append(" ne ''");
        return expression.toString();
    }

    /**
     * 检查表达式是否有效
     *
     * @param expression
     */
    public static Boolean checkExpressionIsEnabled(String expression) {
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


    public static void main(String[] args) {
        ConditionElement element1 = new ConditionElement("((", "skuNo", "contains", "1", "", "and");
        ConditionElement element2 = new ConditionElement("", "platform", "==", "Amazon", "))", "");

        List<ConditionElement> elementList = new ArrayList<>();
        elementList.add(element1);
        elementList.add(element2);
        String expression = SpELRuleUtils.getConditionExpression(elementList);
        System.out.println(expression);
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression1 = parser.parseExpression(expression);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("skuNo", "12");
        jsonObject.set("platform", "Amazon");
        EvaluationContext context = new StandardEvaluationContext(jsonObject);
        boolean result = Boolean.TRUE.equals(expression1.getValue(context, Boolean.class));
        System.out.println("result====" + result);
    }

    /**
     * 检查表达式是否正确
     *
     * @param conditionElementList
     * @return
     */
    public static Boolean checkExpressionIsEnabledByElementList(List<ConditionElement> conditionElementList) {
        String expression = getConditionExpression(conditionElementList);
        return checkExpressionIsEnabled(expression);
    }
}
