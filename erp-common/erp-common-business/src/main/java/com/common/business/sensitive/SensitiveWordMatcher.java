package com.common.business.sensitive;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * sensitive-word DFA 匹配器的轻量封装。
 *
 * <p>业务模块通过本类复用匹配能力，不直接依赖三方 API。</p>
 */
public class SensitiveWordMatcher {

    private final SensitiveWordBs engine;

    private SensitiveWordMatcher(SensitiveWordBs engine) {
        this.engine = engine;
    }

    public static SensitiveWordMatcher of(List<String> words, boolean ignoreCase) {
        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.empty())
                .wordAllow(WordAllows.empty())
                .enableNumCheck(false)
                .enableEmailCheck(false)
                .enableUrlCheck(false)
                .enableIpv4Check(false)
                .ignoreCase(ignoreCase)
                .init();
        if (CollectionUtils.isNotEmpty(words)) {
            List<String> cleanWords = new ArrayList<>(words.size());
            for (String word : words) {
                if (StringUtils.isNotBlank(word)) {
                    cleanWords.add(word.trim());
                }
            }
            if (CollectionUtils.isNotEmpty(cleanWords)) {
                sensitiveWordBs.addWord(cleanWords);
            }
        }
        return new SensitiveWordMatcher(sensitiveWordBs);
    }

    public List<String> findAll(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        List<String> hitList = engine.findAll(text);
        if (CollectionUtils.isEmpty(hitList)) {
            return new ArrayList<>();
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (String hit : hitList) {
            if (StringUtils.isNotBlank(hit)) {
                distinct.add(hit.trim());
            }
        }
        return new ArrayList<>(distinct);
    }
}
