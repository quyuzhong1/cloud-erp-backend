package com.common.core.server.rule;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;

import java.util.List;
import java.util.Map;

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
    SpElExpressionDTO getConditionExpression(List<ConditionElement> conditionElementList, Object object);

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
     * 根据参数进行匹配表达式
     * @param spElDTO
     * @param obj
     * @return
     */
    Boolean matchExpressionWithVariable(SpElExpressionDTO spElDTO,Object obj);


    /**
     * 匹配表达式结果
     * @param conditionList
     * @param obj
     * @return
     */
    Boolean matchExpressionByConditionList(List<ConditionElement> conditionList, Map<String,Object> obj, String key);

    /**
     * 匹配明细表达式结果
     * @param conditionList
     * @param obj
     * @return
     */
    Boolean matchDetailExpressionByConditionList(List<ConditionElement> conditionList, Map<String,Object> obj);

    /**
     * 匹配表达式结果(默认，流程走)
     * @param conditionList
     * @param obj
     * @return
     */
    Boolean matchExpressionDefaultByConditionList(List<ConditionElement> conditionList, Map<String,Object> obj, String key);

    /**
     * 获取字段
     * @author will
     * @date 2024/7/3 15:28
     * @param fieldCode
     * @param mapList
     * @return Object
     */
    Object getByField(String fieldCode, List<Map<String, Object>> mapList);
}
