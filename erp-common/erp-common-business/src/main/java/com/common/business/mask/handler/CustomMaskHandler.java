package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定义正则脱敏：用 {@link MaskContext#getRegex()} 匹配，
 * 命中部分替换为 {@link MaskContext#getReplacement()}
 *
 * <p>例：</p>
 * <ul>
 *   <li>正则 {@code (?<=.{2}).*(?=.{2})} + 替换 {@code *}：京A12345 → 京A***45</li>
 * </ul>
 *
 * <p>双重 ReDoS 防护：</p>
 * <ol>
 *   <li><b>编译时</b>：{@link RegexSafetyGuard#check} 拦截嵌套量词等已知灾难性回溯模式；
 *       不通过的正则编译为 {@link #INVALID} 哨兵，后续直接返回原值；</li>
 *   <li><b>运行时</b>：输入文本超过 {@link RegexSafetyGuard#MAX_INPUT_LEN} 跳过匹配，
 *       把单字段 worst-case CPU 限制在百毫秒内；超过 {@link #SLOW_REGEX_WARN_NS}
 *       的单次匹配 warn 一行（含 className#fieldName），便于在 ELK 抓出问题正则。</li>
 * </ol>
 *
 * <p>正则编译后做 {@link ConcurrentHashMap} 缓存，避免每次请求重复编译。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CustomMaskHandler implements MaskHandler {

    /** 单次匹配耗时告警阈值（纳秒）：超过即认为该正则可能有性能问题 */
    private static final long SLOW_REGEX_WARN_NS = 50L * 1_000_000L;

    /** 正则编译缓存：原始字符串 → 已编译 Pattern */
    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>(64);

    /**
     * 哨兵：表示该字符串编译失败 / 命中安全黑名单，下次直接返回原值，避免重复抛异常
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
        String text = (String) value;
        // 运行时长度护栏：超长输入跳过，避免单字段拖垮整个响应
        if (text.length() > RegexSafetyGuard.MAX_INPUT_LEN) {
            log.warn("CustomMaskHandler skip oversized input: classPath={}, field={}, len={}, threshold={}",
                    ctx.getClassPath(), ctx.getFieldName(), text.length(), RegexSafetyGuard.MAX_INPUT_LEN);
            return text;
        }
        long start = System.nanoTime();
        try {
            return p.matcher(text).replaceAll(ctx.getReplacement());
        } finally {
            long cost = System.nanoTime() - start;
            if (cost >= SLOW_REGEX_WARN_NS) {
                log.warn("REGEX_SLOW classPath={}, field={}, costMs={}, regex={}",
                        ctx.getClassPath(), ctx.getFieldName(), cost / 1_000_000L, regex);
            }
        }
    }

    /**
     * 安全编译：先过 {@link RegexSafetyGuard} 静态检查，命中 ReDoS 模式族直接哨兵化。
     * 即便配置入库时漏掉了校验（外部 RPC 写入 / 旧数据），运行时这里也会拦下。
     */
    private Pattern compileSafe(String regex) {
        String reason = RegexSafetyGuard.check(regex);
        if (reason != null) {
            log.warn("CustomMaskHandler reject unsafe regex: {}, reason={}", regex, reason);
            return INVALID;
        }
        try {
            return Pattern.compile(regex);
        } catch (Exception e) {
            log.warn("CustomMaskHandler regex compile failed: {}, msg={}", regex, e.getMessage());
            return INVALID;
        }
    }
}
