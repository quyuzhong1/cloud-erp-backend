package com.common.business.mask.config;

import com.common.business.mask.cache.CfgMaskWordLocalCache;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * sensitive-word 引擎配置
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>不加载 sensitive-word 内置 6w+ 通用敏感词字典：业务字段名/正常文本极易撞词，且与本框架职责无关；</li>
 *   <li>开启模式检测：手机号（11 位数字，{@code numCheckLen=11}）+ 邮箱 + URL + IPv4，
 *       这是 PII 自动识别的主力来源；</li>
 *   <li>业务自定义词典通过 {@link CfgMaskWordLocalCache#replay()} 从 Redis 缓存灌入。</li>
 * </ul>
 *
 * <p>{@link SensitiveWordBs} 是线程安全的（参考 sensitive-word 文档），全局单例。
 * 后续运维变更由 Redis cache-aside 版本检查触发 {@code addWord/removeWord} 增量更新，不重建对象。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Configuration
public class SensitiveWordBsAutoConfiguration {

    @Resource
    private CfgMaskWordLocalCache cfgMaskWordLocalCache;

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                // 不要内置 6w+ 词典，避免业务字段误伤
                .wordDeny(WordDenys.empty())
                .wordAllow(WordAllows.empty())
                // 模式检测：自动识别手机/邮箱/URL/IP 形态
                .enableNumCheck(true)
                .numCheckLen(11)
                .enableEmailCheck(true)
                .enableUrlCheck(true)
                .enableIpv4Check(true)
                .init();
        log.info("SensitiveWordBs initialized: numCheck=on(len=11), emailCheck=on, urlCheck=on, ipv4Check=on, builtinWordDeny=empty");
        return bs;
    }

    /**
     * Bean 装配完成后回调，把 Redis 缓存里的词典全量重放给引擎。
     */
    @PostConstruct
    public void replayCache() {
        try {
            cfgMaskWordLocalCache.replay();
        } catch (Throwable e) {
            log.warn("SensitiveWordBsAutoConfiguration replay cache failed, engine still works without custom dict", e);
        }
    }
}
