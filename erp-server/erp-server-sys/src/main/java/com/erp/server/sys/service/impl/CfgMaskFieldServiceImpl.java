package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.mask.handler.RegexSafetyGuard;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgMaskFieldDTO;
import com.erp.model.sys.entity.CfgMaskFieldEntity;
import com.erp.server.sys.mapper.CfgMaskFieldMapper;
import com.erp.server.sys.service.CfgMaskFieldService;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置 服务实现
 *
 * <p>所有写操作（add / update / delete）在事务提交后对 Redis 全量缓存做延迟双删；
 * 业务节点下次读取时 Redis miss 后回源 sys 并回填缓存。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class CfgMaskFieldServiceImpl
        extends SuperServiceImpl<CfgMaskFieldMapper, CfgMaskFieldEntity>
        implements CfgMaskFieldService {

    /**
     * Redis 全量缓存 body 体积告警阈值（字节）
     *
     * <p>cfg_mask_field 业务字段配置量本应在百行以内（每行 ~200 字节），
     * 序列化 body 不应超过 256KB。超过则可能配置失控（重复行 / 误填大字段），
     * 同时也会增加 Redis miss 后的反序列化开销。</p>
     */
    private static final int BODY_SIZE_WARN_THRESHOLD = 256 * 1024;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MaskCfgRedisCacheEvictor maskCfgRedisCacheEvictor;

    @Override
    public PagingVO<CfgMaskFieldDTO.ListDTO> paging(PagingDTO<CfgMaskFieldDTO.SearchParamDTO> dto) {
        Page<CfgMaskFieldDTO.ListDTO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgMaskFieldDTO.ListDTO> data = baseMapper.paging(page, dto.getParams());
        return new PagingVO<>(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(CfgMaskFieldDTO.AddDTO dto) {
        validateStrategy(dto.getStrategy());
        validateCustomRegex(dto.getStrategy(), dto.getCustomRegex());
        CfgMaskFieldEntity entity = new CfgMaskFieldEntity();
        BeanUtil.copyProperties(dto, entity);
        if (entity.getDisabled() == null) {
            entity.setDisabled(Boolean.FALSE);
        }
        if (entity.getHideWhenMasked() == null) {
            entity.setHideWhenMasked(Boolean.FALSE);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        boolean ok = this.save(entity);
        if (ok) {
            evictCacheSafely("add");
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgMaskFieldDTO.UpdateDTO dto) {
        validateStrategy(dto.getStrategy());
        validateCustomRegex(dto.getStrategy(), dto.getCustomRegex());
        CfgMaskFieldEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("配置不存在");
        }
        BeanUtil.copyProperties(dto, entity, "id");
        if (entity.getHideWhenMasked() == null) {
            entity.setHideWhenMasked(Boolean.FALSE);
        }
        if (entity.getDisabled() == null) {
            entity.setDisabled(Boolean.FALSE);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        boolean ok = this.updateById(entity);
        if (ok) {
            evictCacheSafely("update");
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(BaseIdsDTO.IdsDTO dto) {
        boolean ok = this.removeByIds(dto.getIds());
        if (ok) {
            evictCacheSafely("delete");
        }
        return ok;
    }

    @Override
    public CfgMaskFieldFullCacheDTO listAllForCache() {
        List<CfgMaskFieldEntity> all = baseMapper.listAllAlive();
        return toFullCache(all);
    }

    @Override
    public Boolean publishFullCache() {
        return doPublish(true);
    }

    /**
     * 触发延迟双删：
     * <ul>
     *   <li>当前线程在事务内 → 注册 {@code afterCommit} 钩子，事务提交后再删缓存；</li>
     *   <li>当前线程不在事务内 → 立即删缓存。</li>
     * </ul>
     * <p>任何分支异常都被吞掉只 warn 一行：DB 已成功 / 即将成功，缓存 miss 后会回源。</p>
     */
    private void evictCacheSafely(String source) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        maskCfgRedisCacheEvictor.doubleDelete(
                                RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY, "cfg_mask_field:" + source);
                    } catch (Throwable e) {
                        log.warn("CfgMaskField redis cache evict after commit failed, but db change is committed", e);
                    }
                }
            });
            return;
        }
        try {
            maskCfgRedisCacheEvictor.doubleDelete(
                    RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY, "cfg_mask_field:" + source);
        } catch (Throwable e) {
            log.warn("CfgMaskField redis cache evict failed, but db change is committed", e);
        }
    }

    private boolean doPublish(boolean throwOnError) {
        try {
            CfgMaskFieldFullCacheDTO payload = listAllForCache();
            String body = JSON.toJSONString(payload);

            if (body.length() > BODY_SIZE_WARN_THRESHOLD) {
                log.warn("CfgMaskField redis cache body too large, size={} bytes, rows={}, "
                                + "considering sharding or filter disabled rows",
                        body.length(), payload.getData().size());
            }

            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY);
            bucket.set(body);

            log.info("CfgMaskField rebuild redis cache ok, rows={}, bodyBytes={}, version={}",
                    payload.getData().size(), body.length(), payload.getVersion());
            return true;
        } catch (Throwable e) {
            log.warn("CfgMaskField rebuild redis cache failed", e);
            if (throwOnError) {
                throw new ServiceException("刷新脱敏配置 Redis 缓存失败：" + e.getMessage());
            }
            return false;
        }
    }

    private CfgMaskFieldFullCacheDTO toFullCache(List<CfgMaskFieldEntity> entities) {
        CfgMaskFieldFullCacheDTO payload = new CfgMaskFieldFullCacheDTO();
        payload.setVersion(System.currentTimeMillis());
        if (entities == null || entities.isEmpty()) {
            payload.setData(new ArrayList<>());
            return payload;
        }
        List<CfgMaskFieldSnapshotEntry> data = entities.stream()
                .map(this::toSnapshot)
                .filter(e -> e != null)
                .collect(Collectors.toList());
        payload.setData(data);
        return payload;
    }

    private CfgMaskFieldSnapshotEntry toSnapshot(CfgMaskFieldEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getClassPath())
                || StringUtils.isBlank(entity.getFieldName()) || StringUtils.isBlank(entity.getStrategy())) {
            return null;
        }
        MaskStrategy strategy = parseStrategy(entity.getStrategy());
        if (strategy == null) {
            return null;
        }
        CfgMaskFieldSnapshotEntry snap = new CfgMaskFieldSnapshotEntry();
        snap.setClassPath(entity.getClassPath());
        snap.setFieldName(entity.getFieldName());
        snap.setStrategy(strategy);
        snap.setRegex(entity.getCustomRegex() == null ? "" : entity.getCustomRegex());
        snap.setReplacement(StringUtils.isBlank(entity.getCustomReplace()) ? "***" : entity.getCustomReplace());
        snap.setPermission(entity.getPermissionCode() == null ? "" : entity.getPermissionCode());
        snap.setSort(entity.getSort() == null ? 0 : entity.getSort());
        snap.setHideWhenMasked(Boolean.TRUE.equals(entity.getHideWhenMasked()));
        return snap;
    }

    private void validateStrategy(String strategy) {
        if (parseStrategy(strategy) == null) {
            throw new ServiceException("非法脱敏策略：" + strategy);
        }
    }

    /**
     * 校验自定义正则的安全性，仅 strategy=CUSTOM 时生效。
     *
     * <p>用 {@link RegexSafetyGuard#checkAndCompile} 同时做：</p>
     * <ol>
     *   <li>ReDoS 黑名单检查（嵌套量词 / 歧义分支 + 量词 / 连续贪婪）</li>
     *   <li>正则编译可行性</li>
     * </ol>
     *
     * <p>fail fast：校验失败直接抛 {@link ServiceException}，根本不让"问题正则"入库，
     * 避免运行时把整个 Tomcat 线程池打满。</p>
     */
    private void validateCustomRegex(String strategy, String regex) {
        MaskStrategy ms = parseStrategy(strategy);
        if (ms != MaskStrategy.CUSTOM) {
            return;
        }
        if (StringUtils.isBlank(regex)) {
            throw new ServiceException("strategy=CUSTOM 时 customRegex 不能为空");
        }
        String reason = RegexSafetyGuard.checkAndCompile(regex);
        if (reason != null) {
            throw new ServiceException("正则不安全：" + reason);
        }
    }

    private MaskStrategy parseStrategy(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        try {
            return MaskStrategy.valueOf(name.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
