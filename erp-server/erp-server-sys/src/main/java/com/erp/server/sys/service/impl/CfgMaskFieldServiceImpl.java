package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgMaskFieldDTO;
import com.erp.model.sys.entity.CfgMaskFieldEntity;
import com.erp.server.sys.mapper.CfgMaskFieldMapper;
import com.erp.server.sys.service.CfgMaskFieldService;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置 服务实现
 *
 * <p>所有写操作（add / update / delete）在事务提交后调用 {@link #publishFullCache()}
 * 触发"先写 Bucket 再 publish"的全量广播流程，与 DorisQuerySettingLocalCache 完全对齐。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class CfgMaskFieldServiceImpl
        extends SuperServiceImpl<CfgMaskFieldMapper, CfgMaskFieldEntity>
        implements CfgMaskFieldService {

    @Resource
    private RedissonClient redissonClient;

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
        CfgMaskFieldEntity exists = findAlive(dto.getClassPath(), dto.getFieldName());
        if (exists != null) {
            throw new ServiceException("已存在相同 (classPath, fieldName) 配置，请改为编辑");
        }
        CfgMaskFieldEntity entity = new CfgMaskFieldEntity();
        BeanUtil.copyProperties(dto, entity);
        if (entity.getEnabled() == null) {
            entity.setEnabled(Boolean.TRUE);
        }
        if (entity.getHideWhenMasked() == null) {
            entity.setHideWhenMasked(Boolean.FALSE);
        }
        boolean ok = this.save(entity);
        if (ok) {
            publishFullCacheSafely();
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgMaskFieldDTO.UpdateDTO dto) {
        validateStrategy(dto.getStrategy());
        CfgMaskFieldEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("配置不存在");
        }
        if (!StringUtils.equals(entity.getClassPath(), dto.getClassPath())
                || !StringUtils.equals(entity.getFieldName(), dto.getFieldName())) {
            CfgMaskFieldEntity duplicate = findAlive(dto.getClassPath(), dto.getFieldName());
            if (duplicate != null && !duplicate.getId().equals(entity.getId())) {
                throw new ServiceException("已存在相同 (classPath, fieldName) 配置");
            }
        }
        BeanUtil.copyProperties(dto, entity, "id");
        if (entity.getHideWhenMasked() == null) {
            entity.setHideWhenMasked(Boolean.FALSE);
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(Boolean.TRUE);
        }
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
    public CfgMaskFieldFullCacheDTO listAllForCache() {
        List<CfgMaskFieldEntity> all = baseMapper.listAllAlive();
        return toFullCache(all);
    }

    @Override
    public Boolean publishFullCache() {
        return doPublish(true);
    }

    private void publishFullCacheSafely() {
        try {
            doPublish(false);
        } catch (Throwable e) {
            // 写表已成功；广播失败时各节点最迟下一次本地缓存重启时也能拿到最新数据
            log.warn("CfgMaskField publishFullCacheSafely failed, but db change is committed", e);
        }
    }

    private boolean doPublish(boolean throwOnError) {
        try {
            CfgMaskFieldFullCacheDTO payload = listAllForCache();
            String body = JSON.toJSONString(payload);

            // 关键顺序：先写 Bucket 再 publish；避免新启动节点拿到的版本旧于已广播版本
            RBucket<String> bucket = redissonClient.getBucket(RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY);
            bucket.set(body);

            RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_FIELD_CFG_REFRESH_CHANNEL);
            topic.publish(body);
            log.info("CfgMaskField publishFullCache ok, size={}, version={}",
                    payload.getData().size(), payload.getVersion());
            return true;
        } catch (Throwable e) {
            log.warn("CfgMaskField publishFullCache failed", e);
            if (throwOnError) {
                throw new ServiceException("广播脱敏配置失败：" + e.getMessage());
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
        snap.setHideWhenMasked(Boolean.TRUE.equals(entity.getHideWhenMasked()));
        return snap;
    }

    private void validateStrategy(String strategy) {
        if (parseStrategy(strategy) == null) {
            throw new ServiceException("非法脱敏策略：" + strategy);
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

    private CfgMaskFieldEntity findAlive(String classPath, String fieldName) {
        if (StringUtils.isBlank(classPath) || StringUtils.isBlank(fieldName)) {
            return null;
        }
        LambdaQueryWrapper<CfgMaskFieldEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(CfgMaskFieldEntity::getClassPath, classPath);
        qw.eq(CfgMaskFieldEntity::getFieldName, fieldName);
        qw.last("LIMIT 1");
        return this.getOne(qw);
    }
}
