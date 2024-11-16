package com.cloud.erp.gateway.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 防sql注入工具类
 * @Author Luo_WG
 * @Date 2023/12/6 14:25
 **/
@Slf4j
public class SqlInjectionRuleUtils {

    private SqlInjectionRuleUtils() {
    }

    /**
     * SQL的正则表达式
     */
    private static String badStrReg = "\\b(and|or)\\b.{1,6}?(=|>|<|\\bin\\b|\\blike\\b)|\\/\\*.+?\\*\\/|<\\s*script\\b|\\bEXEC\\b|UNION.+?SELECT|UPDATE\\s+.+?" +
            "SET|INSERT\\s+INTO.+?VALUES|(SELECT|DELETE)\\s+\\bFROM\\b|(CREATE|ALTER|DROP|TRUNCATE)\\s+(TABLE|DATABASE)";
    
    /**
     * SQL的正则表达式
     */
    private static Pattern sqlPattern = Pattern.compile(badStrReg, Pattern.CASE_INSENSITIVE);
    
    
    /**
     * sql注入校验 map
     *
     * @param map
     * @return
     */
    public static boolean mapRequestSqlKeyWordsCheck(MultiValueMap<String, String> map) {
        if (CollectionUtils.isEmpty(map)) {
            return false;
        }
        //对post请求参数值进行sql注入检验
        return map.entrySet().stream().parallel().filter(Objects::nonNull).anyMatch(entry -> {
            //这里需要将参数转换为小写来处理
            String lowerValue = Optional.ofNullable(entry.getValue())
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse("");
            if (sqlPattern.matcher(lowerValue).find()) {
                log.error("参数[{}]中包含不允许sql的关键词", lowerValue);
                return true;
            }
            return false;
        });
    }
    
    
    /**
     *  sql注入校验 json
     *
     * @param value
     * @return
     */
    public static boolean jsonRequestSqlKeyWordsCheck(String value) {
        if (JSONUtil.isTypeJSONObject(value)) {
            Map<String, Object> map = JSONUtil.parseObj(value);
            if (CollectionUtils.isEmpty(map)) {
                return false;
            }
            //对post请求参数值进行sql注入检验
            return map.entrySet().stream().parallel().anyMatch(entry -> {
                //这里需要将参数转换为小写来处理
                String lowerValue = Optional.ofNullable(entry.getValue())
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .orElse("");
                if (sqlPattern.matcher(lowerValue).find()) {
                    log.error("json参数[{}]中包含不允许sql的关键词", lowerValue);
                    return true;
                }
                return false;
            });
        } else {
            List<Object> list = JSONUtil.parseArray(value);
            //对post请求参数值进行sql注入检验
            return list.stream().parallel().anyMatch(obj -> {
                //这里需要将参数转换为小写来处理
                String lowerValue = Optional.ofNullable(obj)
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .orElse("");
                if (sqlPattern.matcher(lowerValue).find()) {
                    log.error("参数[{}]中包含不允许sql的关键词", lowerValue);
                    return true;
                }
                return false;
            });
        }
    }
}
