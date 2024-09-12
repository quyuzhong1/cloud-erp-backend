package com.common.business.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Redis使用FastJson序列化
 * 
 * @author ruoyi
 */
public class FastJson2LocalDateTimeDeserializer<T> implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String dateStr = parser.getLexer().stringVal();
        if ("0000-00-00 00:00:00".equals(dateStr) || StringUtils.isBlank(dateStr)) {
            return null;
        }
        // 定义日期格式
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        LocalDateTime parse;
        try {
            // 尝试第一种格式
            parse = LocalDateTime.parse(dateStr, formatter1);
        } catch (DateTimeParseException e1) {
            try {
                // 尝试第二种格式
                parse = LocalDateTime.parse(dateStr, formatter2);
            } catch (DateTimeParseException e2) {
                // 如果两种格式都失败，抛出异常或返回 null
                throw new RuntimeException("Failed to parse date: " + dateStr, e2);
            }
        }
        return (T) parse;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}