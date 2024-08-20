package com.common.business.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis使用FastJson序列化
 * 
 * @author ruoyi
 */
public class FastJson2LocalDateTimeDeserializer<T> implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String dateStr = parser.getLexer().stringVal();
        if ("0000-00-00 00:00:00".equals(dateStr)) {
            return null;
        }
        return (T) LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}