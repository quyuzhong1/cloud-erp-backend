package com.common.core.server.rule;
import cn.hutool.json.JSONObject;
import com.common.core.entity.ConditionElement;

import java.util.List;

/**
 * @author Lambda
 * @Classname SpElServer
 * @Description
 * @Date 2023-09-07 8:59
 * @Created by yl
 */
public interface SpElServer {


    /**
     * 获取到sqe 表达式
     *
     * @param conditionElementList
     * @param object
     * @return
     */
    String getConditionExpression(List<ConditionElement> conditionElementList, Object object);

    /**
     * 检查表达式是否正确
     * @param expression
     * @return
     */
    Boolean checkExpressionIsEnabled(String expression);


    /**
     * 匹配表达式结果
     * @param expression
     * @param obj
     * @return
     */
    Boolean matchExpression(String expression,Object obj);


    /**
     * 匹配表达式结果
     * @param conditionList
     * @param obj
     * @return
     */
    Boolean matchExpressionByConditionList(List<ConditionElement> conditionList, JSONObject obj);


}
