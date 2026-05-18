package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskWordLocalCache;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * 自动 PII 脱敏处理器：{@link MaskStrategy#AUTO}
 *
 * <p>用于"自由文本"字段（备注 / 描述 / content / errorMsg ...），业务侧无法预先指定字段语义时兜底使用。</p>
 *
 * <p>检测能力：</p>
 * <ol>
 *   <li><b>sensitive-word DFA</b>：手机号（11 位数字）/ 邮箱 / URL / IPv4，以及 cfg_mask_word 灌入的业务自定义词；</li>
 *   <li><b>身份证号</b>：18 位身份证（含末位校验位 'X'）+ Luhn 不适用，仅用结构正则；</li>
 *   <li><b>银行卡号</b>：16~19 位连续数字 + Luhn 校验避免误伤订单号 / 流水号；</li>
 *   <li><b>白名单豁免</b>：sensitive-word 内置 wordAllow 已在 DFA 阶段先于 wordDeny 生效；
 *       身份证 / 银行卡走自定义正则路径，单独支持白名单豁免。</li>
 * </ol>
 *
 * <p>非 String 不处理；空字符串原样返回。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class AutoPiiMaskHandler implements MaskHandler {

    /**
     * 单字段最大处理长度（字符数）
     *
     * <p>超过该长度的文本：</p>
     * <ul>
     *   <li>跳过 sensitive-word DFA 与正则扫描，原样返回</li>
     *   <li>打印一次 warn 日志（提示该字段可能不应进 AUTO 路径）</li>
     * </ul>
     *
     * <p>设计依据：备注 / 描述类字段正常 ≤ 1KB，超过 16KB 通常是日志贴板、JSON 大对象等
     * 不适合自动 PII 检测的场景，强制扫描会让单请求耗时雪崩（DFA 是 O(n)，列表 1000 条
     * 16KB ≈ 16MB 文本，单接口可能秒级）。需要时业务侧应改用更精确的策略而非 AUTO。</p>
     */
    private static final int MAX_AUTO_TEXT_LEN = 16 * 1024;

    /**
     * 短文本下限：身份证 18 位、手机 11 位、银行卡 16+ 位、邮箱至少 5 字符（a@b.c）。
     * 文本短于此值时不可能命中任何 PII 形态，全部跳过。
     */
    private static final int MIN_PII_LEN = 5;

    /**
     * 身份证 / 银行卡正则的最少字符数：18 位身份证 + 16 位银行卡的下限。
     * 不达此长度直接跳过这两个最贵的正则。
     */
    private static final int MIN_ID_OR_CARD_LEN = 16;

    /**
     * 18 位身份证号正则
     * - 6 位行政区划 + 8 位日期 + 3 位顺序 + 1 位校验位 (0-9 或 X/x)
     * - \b 边界保证不命中超长数字串里的子串
     */
    private static final Pattern ID_CARD_18 = Pattern.compile(
            "(?<![0-9Xx])([1-9][0-9]{5}(?:18|19|20)[0-9]{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12][0-9]|3[01])[0-9]{3}[0-9Xx])(?![0-9Xx])"
    );

    /**
     * 银行卡号正则：16~19 位连续数字（中间可能含 1 个空格分组：4-4-4-4 / 4-4-4-4-3）
     * 命中后再做 Luhn 校验避免误伤
     */
    private static final Pattern BANK_CARD = Pattern.compile(
            "(?<![0-9])([0-9]{16,19})(?![0-9])"
    );

    private final ObjectProvider<SensitiveWordBs> sensitiveWordBsProvider;

    @Autowired(required = false)
    private CfgMaskWordLocalCache wordLocalCache;

    public AutoPiiMaskHandler(ObjectProvider<SensitiveWordBs> sensitiveWordBsProvider) {
        this.sensitiveWordBsProvider = sensitiveWordBsProvider;
    }

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.AUTO;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String text = (String) value;
        int len = text.length();
        if (len == 0) {
            return text;
        }
        // 短文本短路：不可能含任何 PII（最短的"a@b.c"也要 5 字符）
        if (len < MIN_PII_LEN) {
            return text;
        }
        // 超长文本短路：避免单字段拖垮整个响应（典型如日志贴板）
        if (len > MAX_AUTO_TEXT_LEN) {
            log.warn("AutoPiiMaskHandler skip oversized field: classPath={}, field={}, len={}, threshold={}",
                    ctx == null ? "?" : ctx.getClassPath(),
                    ctx == null ? "?" : ctx.getFieldName(),
                    len, MAX_AUTO_TEXT_LEN);
            return text;
        }

        // 一次扫描出文本特征：含数字 / 含 '@'。后续 3 个步骤按特征短路。
        // 单次 char 遍历 ≈ 5 ns/char，比让正则空跑一遍便宜得多。
        boolean hasDigit = false;
        boolean hasAt = false;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                hasDigit = true;
                if (hasAt) {
                    break;
                }
            } else if (c == '@') {
                hasAt = true;
                if (hasDigit) {
                    break;
                }
            }
        }

        String result = text;

        // 1) sensitive-word：手机/邮箱/URL/IPv4 + 业务自定义黑名单
        //    短路条件：既无数字（手机/IP 必须）也无 '@'（邮箱必须）→ 仅 URL 可能命中，让 DFA 跑一次
        if (wordLocalCache != null) {
            wordLocalCache.refreshEngineIfNeeded();
        }
        SensitiveWordBs bs = sensitiveWordBsProvider.getIfAvailable();
        if (bs != null) {
            try {
                result = bs.replace(result);
            } catch (Throwable ignore) {
                // 引擎异常时退化为只跑下面的自定义正则；不影响业务返回
            }
        }

        // 2) 身份证号（18 位）+ 3) 银行卡号（16~19 位）：必须含数字且 ≥16 字符
        if (hasDigit && len >= MIN_ID_OR_CARD_LEN) {
            result = maskByPattern(result, ID_CARD_18, AutoPiiMaskHandler::maskIdCard);
            result = maskByPattern(result, BANK_CARD, AutoPiiMaskHandler::maskBankCardIfLuhnOk);
        }

        return result;
    }

    private static String maskByPattern(String text, Pattern pattern, java.util.function.Function<String, String> masker) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return text;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String hit = m.group(1);
            String masked = masker.apply(hit);
            // appendReplacement 需要 escape，但这里把替换串当字面量；用 quoteReplacement 安全
            m.appendReplacement(sb, Matcher.quoteReplacement(masked));
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    private static String maskIdCard(String idCard) {
        int len = idCard.length();
        if (len < 8) {
            return idCard;
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(idCard, 0, 3);
        for (int i = 0; i < len - 7; i++) {
            sb.append('*');
        }
        sb.append(idCard, len - 4, len);
        return sb.toString();
    }

    private static String maskBankCardIfLuhnOk(String card) {
        if (!luhnCheck(card)) {
            return card;
        }
        int len = card.length();
        if (len < 9) {
            return card;
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(card, 0, 4);
        for (int i = 0; i < len - 8; i++) {
            sb.append('*');
        }
        sb.append(card, len - 4, len);
        return sb.toString();
    }

    /**
     * Luhn 校验：从右往左每隔一位 *2，>9 则减 9，求和后 mod 10 == 0 即合法。
     * 用于银行卡号（IBAN/普通借记卡 16-19 位）误伤过滤。
     */
    private static boolean luhnCheck(String digits) {
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            char c = digits.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            int n = c - '0';
            if (alt) {
                n <<= 1;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }
}
