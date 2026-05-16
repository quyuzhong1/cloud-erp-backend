package com.common.business.mask.handler;

import java.util.regex.Pattern;

/**
 * 正则安全护栏：阻止"灾难性回溯"模式（ReDoS, Regex Denial of Service）
 *
 * <p>背景：Java {@code java.util.regex} 是 NFA 引擎，遇到歧义量词会做指数级回溯。
 * 一个简单的 {@code (a+)+} 配上 30 字符的恶意输入即可让 CPU 100% 卡 8 秒，
 * 从而拖垮整个 Tomcat 线程池。本类提供两道闸门：</p>
 *
 * <ol>
 *   <li><b>静态分析</b>（{@link #check(String)}）：用启发式规则检测已知 ReDoS 模式族。
 *       在配置入库时调用，<i>fail fast</i>：发现可疑正则直接拒绝，根本不让它进系统；</li>
 *   <li><b>运行时长度护栏</b>（{@link #MAX_INPUT_LEN}）：即便有"漏网之鱼"，输入文本超过
 *       8KB 时拒绝匹配，把单字段 worst-case CPU 限制在百毫秒内。</li>
 * </ol>
 *
 * <p>检测覆盖的常见 ReDoS 模式族（来自 OWASP & 学术综述）：</p>
 * <ul>
 *   <li>嵌套量词：{@code (a+)+} / {@code (a*)*} / {@code (a+)*} / {@code (a*)+}</li>
 *   <li>歧义分支 + 量词：{@code (a|a)+} / {@code (a|ab)+}</li>
 *   <li>"贪婪 + 可选"组合：{@code a*a*} / {@code .*.+}</li>
 * </ul>
 *
 * <p>检测策略偏保守：宁可误杀少数低风险写法（让运维换更明确的写法），不放过潜在 ReDoS。
 * 业务正常的字段脱敏正则（如 {@code (?<=.{3}).*(?=.{4})}、{@code [0-9]{4}}）不会命中。</p>
 *
 * @author cloud-erp
 */
public final class RegexSafetyGuard {

    private RegexSafetyGuard() {
    }

    /**
     * CUSTOM 策略允许处理的最大输入文本长度（字符）。
     * <p>超过即跳过匹配返回原文（同 {@code AutoPiiMaskHandler} 同样语义，只是阈值更紧
     * 因为 CUSTOM 正则是用户提供的，复杂度未知）。</p>
     */
    public static final int MAX_INPUT_LEN = 8 * 1024;

    /**
     * 单条正则字符串最大长度。超过即视为可疑（正常字段脱敏正则不应超过 256 字符）。
     */
    public static final int MAX_REGEX_LEN = 256;

    /**
     * ① 嵌套量词：(... + ...) + / (... * ...) + / (... + ...) * 等。
     * <p>说明：粗略匹配 {@code (...)} 后紧跟 {@code +/*} 且括号内含 {@code +/*}。</p>
     */
    private static final Pattern NESTED_QUANTIFIER = Pattern.compile(
            "\\([^()]*[+*][^()]*\\)\\s*[+*]"
    );

    /**
     * ② 歧义分支 + 量词：(a|a)+ / (a|ab)+ / (\\w|\\w)+ 等
     * <p>简化匹配：{@code (X|Y)} 紧跟 {@code +/*}，且 X / Y 任一是另一前缀的常见情况</p>
     */
    private static final Pattern AMBIGUOUS_ALTERNATION = Pattern.compile(
            "\\([^()|]+\\|[^()|]+\\)\\s*[+*]"
    );

    /**
     * ③ 连续贪婪量词：{@code a*a*} / {@code .*.+} / {@code \\w+\\w+}
     */
    private static final Pattern CONSECUTIVE_GREEDY = Pattern.compile(
            "(?:\\\\?.|\\[[^]]+])[+*][^?+*({]\\s*(?:\\\\?.|\\[[^]]+])[+*]"
    );

    /**
     * 检查给定正则字符串是否包含已知的 ReDoS 模式。
     *
     * @param regex 待检查的正则字符串
     * @return null 表示安全；非 null 是不通过原因（用于异常消息和审计日志）
     */
    public static String check(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        if (regex.length() > MAX_REGEX_LEN) {
            return "正则长度 " + regex.length() + " 超过上限 " + MAX_REGEX_LEN
                    + "（业务正则不应这么复杂，请拆分或换策略）";
        }
        if (NESTED_QUANTIFIER.matcher(regex).find()) {
            return "存在嵌套量词模式（如 (a+)+ / (a*)*），属灾难性回溯高风险写法，已拒绝";
        }
        if (AMBIGUOUS_ALTERNATION.matcher(regex).find()) {
            return "存在歧义分支 + 量词模式（如 (a|a)+ / (a|ab)+），属灾难性回溯高风险写法，已拒绝";
        }
        if (CONSECUTIVE_GREEDY.matcher(regex).find()) {
            return "存在连续贪婪量词模式（如 a*a* / .*.+），可能引发性能问题，已拒绝";
        }
        return null;
    }

    /**
     * 静态分析 + 编译可行性双重校验。常用于"配置入库前"的最严校验。
     *
     * @return null 表示安全；非 null 是不通过原因
     */
    public static String checkAndCompile(String regex) {
        String reason = check(regex);
        if (reason != null) {
            return reason;
        }
        try {
            Pattern.compile(regex);
            return null;
        } catch (Exception e) {
            return "正则编译失败：" + e.getMessage();
        }
    }
}
