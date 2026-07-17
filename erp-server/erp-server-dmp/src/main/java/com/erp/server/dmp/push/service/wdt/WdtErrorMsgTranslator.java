package com.erp.server.dmp.push.service.wdt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.server.dmp.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 旺店通错误信息翻译：仅对<b>纯英文类</b>报错做关键字映射，展示为【中文说明】原始信息：{原文}；
 * 含中文（含「中文 + 仓位/SKU 编码」）或未命中映射时直接返回旺店通原文。
 * <p>映射规则维护在 dict_basic.type = {@link #DICT_TYPE}，remark = KEYWORD，name 为英文关键字。
 * 不使用错误码（CODE）映射。</p>
 * <p><b>使用范围</b>：当前仅由 {@link WdtWarnMsgHelper} 在 MQ 告警 keyInfo 中调用；
 * 推送 Service 层 {@code ServiceException} 文案不在此翻译。</p>
 */
@Slf4j
@Component
public class WdtErrorMsgTranslator {

    /** dict_basic.type：旺店通错误信息映射（仅英文关键字） */
    public static final String DICT_TYPE = "wdtErrorMsgMapping";
    /** remark=KEYWORD：按原文包含英文关键字匹配，name 存英文关键字 */
    public static final String MATCH_TYPE_KEYWORD = "KEYWORD";

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 翻译旺店通错误：含英文且命中字典英文关键字 → 【中文说明】原始信息：{原文}；否则返回原文。
     *
     * @param wdtErrorCode 旺店通 API 返回的错误码（如 response.status / data.status），
     *                     <b>当前不参与映射逻辑</b>，仅用于异常日志上下文；翻译仅依据 {@code rawMsg} 与字典 KEYWORD 匹配
     * @param rawMsg       旺店通返回的原始错误信息
     */
    public String translate(Integer wdtErrorCode, String rawMsg) {
        try {
            List<DictBasicEntity> mappings = loadEnabledMappings();
            return resolveDisplayMsg(rawMsg, mappings);
        } catch (Exception e) {
            log.warn("旺店通错误信息翻译失败，回退原文: wdtErrorCode={}, rawMsg={}", wdtErrorCode, rawMsg, e);
            return CharSequenceUtil.nullToEmpty(rawMsg).trim();
        }
    }

    /**
     * 解析对人展示的错误信息（供单测及告警文案组装）。
     */
    public static String resolveDisplayMsg(String rawMsg, List<DictBasicEntity> mappings) {
        String raw = CharSequenceUtil.nullToEmpty(rawMsg).trim();
        if (CharSequenceUtil.isBlank(raw) || !shouldTryEnglishMapping(raw)) {
            return raw;
        }
        String cnDesc = matchKeyword(raw, mappings);
        if (CharSequenceUtil.isNotBlank(cnDesc)) {
            return formatDisplayMsg(cnDesc, raw);
        }
        return raw;
    }

    /**
     * 组装展示文案：【中文说明】原始信息：{原文}
     */
    public static String formatDisplayMsg(String cnDesc, String rawMsg) {
        String safeCn = CharSequenceUtil.nullToEmpty(cnDesc).trim();
        String safeRaw = CharSequenceUtil.nullToEmpty(rawMsg).trim();
        return CharSequenceUtil.format("【{}】原始信息：{}", safeCn, safeRaw);
    }

    /**
     * 是否应对该错误尝试英文关键字映射：不含中文，且含英文字母。
     * <p>「货位不存在 A-01-01」「货位不存在 WH001」等含中文的混合文案返回 false，直接走原文。</p>
     */
    public static boolean shouldTryEnglishMapping(String rawMsg) {
        if (CharSequenceUtil.isBlank(rawMsg)) {
            return false;
        }
        if (containsCjk(rawMsg)) {
            return false;
        }
        return containsAsciiLetter(rawMsg);
    }

    /**
     * 是否包含 CJK 统一表意文字（中文等）。
     */
    public static boolean containsCjk(String rawMsg) {
        if (CharSequenceUtil.isBlank(rawMsg)) {
            return false;
        }
        for (int i = 0; i < rawMsg.length(); i++) {
            if (Character.UnicodeScript.of(rawMsg.charAt(i)) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAsciiLetter(String rawMsg) {
        for (int i = 0; i < rawMsg.length(); i++) {
            char ch = rawMsg.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                return true;
            }
        }
        return false;
    }

    private List<DictBasicEntity> loadEnabledMappings() {
        List<DictBasicEntity> list = dictBasicService.getByKey(DICT_TYPE);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream()
                .filter(item -> item.getStatus() == null || Boolean.TRUE.equals(item.getStatus()))
                .filter(item -> MATCH_TYPE_KEYWORD.equalsIgnoreCase(CharSequenceUtil.nullToEmpty(item.getRemark())))
                .filter(item -> CharSequenceUtil.isNotBlank(item.getName()) && CharSequenceUtil.isNotBlank(item.getValue()))
                .filter(item -> containsAsciiLetter(item.getName()))
                .collect(Collectors.toList());
    }

    private static String matchKeyword(String rawMsg, List<DictBasicEntity> mappings) {
        if (CollUtil.isEmpty(mappings)) {
            return null;
        }
        String lowerRaw = rawMsg.toLowerCase(Locale.ROOT);
        return mappings.stream()
                .sorted(Comparator
                        .comparing((DictBasicEntity item) -> CharSequenceUtil.nullToEmpty(item.getName()).length(), Comparator.reverseOrder())
                        .thenComparing(item -> item.getSort() == null ? 0 : item.getSort(), Comparator.reverseOrder()))
                .map(item -> {
                    String keyword = CharSequenceUtil.nullToEmpty(item.getName());
                    if (lowerRaw.contains(keyword.toLowerCase(Locale.ROOT))) {
                        return item.getValue();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
