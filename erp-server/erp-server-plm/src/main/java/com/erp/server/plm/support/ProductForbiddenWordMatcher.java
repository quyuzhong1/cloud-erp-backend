package com.erp.server.plm.support;

import com.common.business.sensitive.SensitiveWordMatcher;
import com.common.business.sensitive.SensitiveWordMatcher;
import com.erp.server.plm.mapper.CfgProductForbiddenWordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 产品违禁词独立匹配器。
 *
 * <p>复用 sensitive-word 的 DFA 匹配能力，但不复用脱敏框架全局 SensitiveWordBs，
 * 避免产品违禁词污染出参脱敏，也避免 cfg_mask_word 影响产品保存拦截。</p>
 */
@Slf4j
@Component
public class ProductForbiddenWordMatcher {

    private static final long VERSION_CHECK_INTERVAL_MS = 1000L;

    @Resource
    private CfgProductForbiddenWordMapper cfgProductForbiddenWordMapper;

    private volatile SensitiveWordMatcher engine = newEngine(new ArrayList<>());

    private volatile LocalDateTime lastMaxUpdateTime;

    private volatile long lastCheckMillis;

    public List<String> match(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        refreshIfNeeded();
        return engine.findAll(text);
    }

    public List<String> match(String text, List<String> enabledWords) {
        if (StringUtils.isBlank(text) || CollectionUtils.isEmpty(enabledWords)) {
            return new ArrayList<>();
        }
        return openSnapshot(enabledWords).findAll(text);
    }

    /**
     * 基于当前启用词库构建一次性匹配器，供全量检测等批处理场景复用。
     */
    public SensitiveWordMatcher openSnapshot() {
        refreshIfNeeded();
        return newEngine(cfgProductForbiddenWordMapper.listEnabledWords());
    }

    /**
     * 基于指定词库构建一次性匹配器。
     */
    public static SensitiveWordMatcher openSnapshot(List<String> enabledWords) {
        return newEngine(enabledWords);
    }

    public void refreshNow() {
        synchronized (this) {
            rebuildEngine();
            lastCheckMillis = System.currentTimeMillis();
        }
    }

    private void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
            return;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - lastCheckMillis < VERSION_CHECK_INTERVAL_MS) {
                return;
            }
            lastCheckMillis = now;
            LocalDateTime maxUpdateTime = cfgProductForbiddenWordMapper.getEnabledWordsMaxUpdateTime();
            if (!Objects.equals(maxUpdateTime, lastMaxUpdateTime)) {
                rebuildEngine(maxUpdateTime);
            }
        }
    }

    private void rebuildEngine() {
        LocalDateTime maxUpdateTime = cfgProductForbiddenWordMapper.getEnabledWordsMaxUpdateTime();
        rebuildEngine(maxUpdateTime);
    }

    private void rebuildEngine(LocalDateTime maxUpdateTime) {
        List<String> words = cfgProductForbiddenWordMapper.listEnabledWords();
        this.engine = newEngine(words);
        this.lastMaxUpdateTime = maxUpdateTime;
        log.info("ProductForbiddenWordMatcher refreshed, enabledWordSize={}, maxUpdateTime={}",
                CollectionUtils.isEmpty(words) ? 0 : words.size(), maxUpdateTime);
    }

    private static SensitiveWordMatcher newEngine(List<String> words) {
        return SensitiveWordMatcher.of(words, true);
    }
}
