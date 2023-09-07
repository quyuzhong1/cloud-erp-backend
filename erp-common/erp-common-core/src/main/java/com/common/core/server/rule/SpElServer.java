package com.common.core.server.rule;/**
 * @author Lambda
 * @Classname SpElServer
 * @Description TODO
 * @Date 2023-09-07 8:59
 * @Created by yl
 */

import com.common.core.rule.ConditionElement;

import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-07 8:59
 */
public interface SpElServer {


    /**
     * 获取到sqe 表达式
     * @param conditionElementList
     * @param object
     * @return
     */
    String getConditionExpression(List<ConditionElement> conditionElementList,Object object);


}
