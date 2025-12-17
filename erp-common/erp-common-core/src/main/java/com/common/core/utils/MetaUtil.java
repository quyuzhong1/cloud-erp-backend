package com.common.core.utils;

import cn.hutool.core.util.ReUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 币种工具类
 *
 * @author Jim
 * @since 2024-12-18
 */
@Slf4j
public class MetaUtil {

    /**
     * 从 MetaObject 中获取 isUserSystem 字段的值
     */
    public static Boolean getIsUserSystem(MetaObject metaObject) {
        Object value = null;
        try {
            // 先尝试获取 isUserSystem 字段
            if (metaObject.hasGetter("isUserSystem")) {
                value = metaObject.getValue("isUserSystem");
            }
        } catch (Exception e) {
            // 如果获取字段时出现异常，忽略并返回 false
            return false;
        }

        // 如果值为 Boolean 类型直接返回，否则返回 false
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }
}
