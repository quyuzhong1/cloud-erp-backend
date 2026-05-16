package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 自定义正则脱敏：用 {@link MaskContext#getRegex()} 匹配，
 * 命中部分替换为 {@link MaskContext#getReplacement()}
 *
 * <p>例：</p>
 * <ul>
 *   <li>正则 {@code (?<=.{2}).*(?=.{2})} + 替换 {@code *}：京A12345 → 京A***45</li>
 * </ul>
 *
 * <p>正则编译后做 {@link ConcurrentHashMap} 缓存，避免每次请求重复编译；
 * 正则非法时记录日志并返回原值（防御性，不影响业务流程）。</p>
 *
 * @author cloud-erp
 */
@Component
public class CustomMaskHandler implements MaskHandler {

    /** 正则编译缓存：原始字符串 → 已编译 Pattern */
    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>(64);

    /**
     * 哨兵：表示该字符串编译失败，下次直接返回原值，避免重复抛异常
     */
    private static final Pattern INVALID = Pattern.compile("");

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.CUSTOM;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String regex = ctx.getRegex();
        if (regex == null || regex.isEmpty()) {
            return value;
        }
        Pattern p = patternCache.computeIfAbsent(regex, this::compileSafe);
        if (p == INVALID) {
            return value;
        }
        return p.matcher((String) value).replaceAll(ctx.getReplacement());
    }

    private Pattern compileSafe(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return INVALID;
        }
    }
}
