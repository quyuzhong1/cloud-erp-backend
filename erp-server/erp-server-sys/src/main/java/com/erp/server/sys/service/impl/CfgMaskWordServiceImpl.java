package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.business.mask.cache.CfgMaskWordSnapshotEntry;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgMaskWordDTO;
import com.erp.model.sys.entity.CfgMaskWordEntity;
import com.erp.server.sys.mapper.CfgMaskWordMapper;
import com.erp.server.sys.service.CfgMaskWordService;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
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
 * 脱敏词典 服务实现
 *
 * <p>所有写操作（add / update / delete）在事务提交后调用 {@link #publishFullCache()}
 * 触发"先写 Bucket 再 publish"的全量广播流程，与 {@code CfgMaskFieldServiceImpl} 完全对齐。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class CfgMaskWordServiceImpl
        extends SuperServiceImpl<CfgMaskWordMapper, CfgMaskWordEntity>
        implements CfgMaskWordService {

    /**
     * publish body 体积告警阈值（字节）
     *
     * <p>词典正常规模在百~千词（每词 ~30 字节），body 不应超过 1MB。超过则可能：</p>
     * <ul>
     *   <li>运营误把通用敏感词字典批量灌入；</li>
     *   <li>词典持续增长未做生命周期治理。</li>
     * </ul>
     * <p>大 body 会让每个节点的反序列化 + 集合 diff 都变慢，且
     * {@code SensitiveWordBs.addWord/removeWord} 需要重建 DFA 节点。</p>
     */
    private static final int BODY_SIZE_WARN_THRESHOLD = 1024 * 1024;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public PagingVO<CfgMaskWordDTO.ListDTO> paging(PagingDTO<CfgMaskWordDTO.SearchParamDTO> dto) {
        Page<CfgMaskWordDTO.ListDTO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgMaskWordDTO.ListDTO> data = baseMapper.paging(page, dto.getParams());
        return new PagingVO<>(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(CfgMaskWordDTO.AddDTO dto) {
        validateWordType(dto.getWordType());
        CfgMaskWordEntity exists = findAlive(dto.getWordType(), dto.getWord());
        if (exists != null) {
            throw new ServiceException("已存在相同 (wordType, word) 词典条目，请改为编辑");
        }
        CfgMaskWordEntity entity = new CfgMaskWordEntity();
        BeanUtil.copyProperties(dto, entity);
        if (entity.getEnabled() == null) {
            entity.setEnabled(Boolean.TRUE);
        }
        boolean ok = this.save(entity);
        if (ok) {
            publishFullCacheSafely();
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgMaskWordDTO.UpdateDTO dto) {
        validateWordType(dto.getWordType());
        CfgMaskWordEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("词典条目不存在");
        }
        boolean wordChanged = !StringUtils.equals(entity.getWord(), dto.getWord())
                || !java.util.Objects.equals(entity.getWordType(), dto.getWordType());
        if (wordChanged) {
            CfgMaskWordEntity duplicate = findAlive(dto.getWordType(), dto.getWord());
            if (duplicate != null && !duplicate.getId().equals(entity.getId())) {
                throw new ServiceException("已存在相同 (wordType, word) 词典条目");
            }
        }
        BeanUtil.copyProperties(dto, entity, "id");
        boolean ok = this.updateById(entity);
        if (ok) {
            publishFullCacheSafely();
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(BaseIdsDTO.IdsDTO dto) {
        boolean ok = this.removeByIds(dto.getIds());
        if (ok) {
            publishFullCacheSafely();
        }
        return ok;
    }

    @Override
    public CfgMaskWordFullCacheDTO listAllForCache() {
        List<CfgMaskWordEntity> all = baseMapper.listAllAlive();
        return toFullCache(all);
    }

    @Override
    public Boolean publishFullCache() {
        return doPublish(true);
    }

    /**
     * 触发广播：在事务内则延迟到 {@code afterCommit}，否则立即 publish。
     * 见 {@code CfgMaskFieldServiceImpl#publishFullCacheSafely} 的同款说明。
     */
    private void publishFullCacheSafely() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        doPublish(false);
                    } catch (Throwable e) {
                        log.warn("CfgMaskWord publish after commit failed, but db change is committed", e);
                    }
                }
            });
            return;
        }
        try {
            doPublish(false);
        } catch (Throwable e) {
            log.warn("CfgMaskWord publishFullCacheSafely failed, but db change is committed", e);
        }
    }

    private boolean doPublish(boolean throwOnError) {
        try {
            CfgMaskWordFullCacheDTO payload = listAllForCache();
            String body = JSON.toJSONString(payload);

            if (body.length() > BODY_SIZE_WARN_THRESHOLD) {
                log.warn("CfgMaskWord publishFullCache body too large, size={} bytes, words={}, "
                                + "consider word lifecycle review",
                        body.length(), payload.getData().size());
            }

            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_WORD_CFG_FULL_KEY);
            bucket.set(body);

            RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_WORD_CFG_REFRESH_CHANNEL);
            topic.publish(body);
            log.info("CfgMaskWord publishFullCache ok, words={}, bodyBytes={}, version={}",
                    payload.getData().size(), body.length(), payload.getVersion());
            return true;
        } catch (Throwable e) {
            log.warn("CfgMaskWord publishFullCache failed", e);
            if (throwOnError) {
                throw new ServiceException("广播脱敏词典失败：" + e.getMessage());
            }
            return false;
        }
    }

    private CfgMaskWordFullCacheDTO toFullCache(List<CfgMaskWordEntity> entities) {
        CfgMaskWordFullCacheDTO payload = new CfgMaskWordFullCacheDTO();
        payload.setVersion(System.currentTimeMillis());
        if (entities == null || entities.isEmpty()) {
            payload.setData(new ArrayList<>());
            return payload;
        }
        List<CfgMaskWordSnapshotEntry> data = entities.stream()
                .map(this::toSnapshot)
                .filter(e -> e != null)
                .collect(Collectors.toList());
        payload.setData(data);
        return payload;
    }

    private CfgMaskWordSnapshotEntry toSnapshot(CfgMaskWordEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getWord()) || entity.getWordType() == null) {
            return null;
        }
        if (entity.getWordType() != CfgMaskWordSnapshotEntry.WORD_TYPE_DENY
                && entity.getWordType() != CfgMaskWordSnapshotEntry.WORD_TYPE_ALLOW) {
            return null;
        }
        return new CfgMaskWordSnapshotEntry(entity.getWordType(), entity.getWord());
    }

    private void validateWordType(Integer wordType) {
        if (wordType == null) {
            throw new ServiceException("词类型不能为空");
        }
        if (wordType != CfgMaskWordSnapshotEntry.WORD_TYPE_DENY
                && wordType != CfgMaskWordSnapshotEntry.WORD_TYPE_ALLOW) {
            throw new ServiceException("非法词类型：" + wordType + "（仅支持 0=黑名单 / 1=白名单）");
        }
    }

    private CfgMaskWordEntity findAlive(Integer wordType, String word) {
        if (wordType == null || StringUtils.isBlank(word)) {
            return null;
        }
        LambdaQueryWrapper<CfgMaskWordEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(CfgMaskWordEntity::getWordType, wordType);
        qw.eq(CfgMaskWordEntity::getWord, word);
        qw.last("LIMIT 1");
        return this.getOne(qw);
    }
}
