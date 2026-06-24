package com.erp.server.sys.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysApiTokenWhitelistDTO;
import com.erp.model.sys.entity.SysApiTokenWhitelistEntity;
import com.erp.server.sys.mapper.SysApiTokenWhitelistMapper;
import com.erp.server.sys.service.SysApiTokenWhitelistService;
import com.erp.server.sys.support.SysApiTokenSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * API Token 接口白名单 服务实现类
 * </p>
 */
@Slf4j
@Service
public class SysApiTokenWhitelistServiceImpl extends SuperServiceImpl<SysApiTokenWhitelistMapper, SysApiTokenWhitelistEntity> implements SysApiTokenWhitelistService {

    /**
     * 本地缓存只用于降低高频认证时的白名单查询压力；多实例配置变更最多等待该 TTL 收敛。
     */
    private static final long CACHE_TTL_MILLIS = 30 * 1000L;

    /**
     * 只锁缓存重建，不锁正常匹配路径，避免每次认证进入同步块。
     */
    private final Object whitelistCacheLock = new Object();

    /**
     * volatile 保证写操作清缓存和缓存重建对认证线程立即可见。
     */
    private volatile WhitelistCache whitelistCache;

    @Override
    public List<SysApiTokenWhitelistDTO.ListDTO> listConfig() {
        return this.lambdaQuery()
                .orderByDesc(SysApiTokenWhitelistEntity::getCreateTime)
                .list()
                .stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysApiTokenWhitelistDTO.AddDTO dto) {
        String pathPattern = SysApiTokenSupport.normalizePathPattern(dto.getPathPattern());
        checkDuplicate(pathPattern, null);

        SysApiTokenWhitelistEntity entity = new SysApiTokenWhitelistEntity();
        entity.setPathPattern(pathPattern);
        Boolean result = this.save(entity);
        if (Boolean.TRUE.equals(result)) {
            invalidateCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysApiTokenWhitelistDTO.UpdateDTO dto) {
        SysApiTokenWhitelistEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("接口白名单配置不存在");
        }

        String pathPattern = SysApiTokenSupport.normalizePathPattern(dto.getPathPattern());
        checkDuplicate(pathPattern, dto.getId());
        entity.setPathPattern(pathPattern);
        Boolean result = this.updateById(entity);
        if (Boolean.TRUE.equals(result)) {
            invalidateCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeConfig(BaseIdDTO dto) {
        SysApiTokenWhitelistEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("接口白名单配置不存在");
        }
        Boolean result = this.removeById(dto.getId());
        if (Boolean.TRUE.equals(result)) {
            invalidateCache();
        }
        return result;
    }

    @Override
    public Boolean match(String requestPath) {
        String normalizedRequestPath;
        try {
            normalizedRequestPath = SysApiTokenSupport.normalizePathPattern(requestPath);
        } catch (Exception e) {
            log.warn("API Token白名单请求路径不合法: {}", requestPath);
            return false;
        }

        for (String pathPattern : getCachedPathPatterns()) {
            if (StringUtils.isBlank(pathPattern)) {
                continue;
            }
            try {
                if (SysApiTokenSupport.matchPathPattern(pathPattern, normalizedRequestPath)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("API Token白名单路径模式不合法, pathPattern={}", pathPattern);
            }
        }
        return false;
    }

    private void checkDuplicate(String pathPattern, String excludeId) {
        int count = this.lambdaQuery()
                .eq(SysApiTokenWhitelistEntity::getPathPattern, pathPattern)
                .ne(StringUtils.isNotBlank(excludeId), SysApiTokenWhitelistEntity::getId, excludeId)
                .count();
        if (count > 0) {
            throw new ServiceException("接口路径已存在");
        }
    }

    private SysApiTokenWhitelistDTO.ListDTO toListDTO(SysApiTokenWhitelistEntity entity) {
        SysApiTokenWhitelistDTO.ListDTO dto = new SysApiTokenWhitelistDTO.ListDTO();
        dto.setId(entity.getId());
        dto.setPathPattern(entity.getPathPattern());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    private List<String> getCachedPathPatterns() {
        long now = System.currentTimeMillis();
        WhitelistCache cache = whitelistCache;
        if (cache != null && cache.expireTimeMillis > now) {
            return cache.pathPatterns;
        }

        synchronized (whitelistCacheLock) {
            cache = whitelistCache;
            now = System.currentTimeMillis();
            if (cache != null && cache.expireTimeMillis > now) {
                return cache.pathPatterns;
            }
            List<String> pathPatterns = this.lambdaQuery()
                    .select(SysApiTokenWhitelistEntity::getPathPattern)
                    .list()
                    .stream()
                    .map(SysApiTokenWhitelistEntity::getPathPattern)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            // 多实例配置变更靠短 TTL 收敛，本实例写操作会主动清理缓存。
            whitelistCache = new WhitelistCache(Collections.unmodifiableList(pathPatterns), now + CACHE_TTL_MILLIS);
            return whitelistCache.pathPatterns;
        }
    }

    private void invalidateCache() {
        // 新增/修改/删除成功后清理本实例缓存，避免本机继续使用旧白名单。
        whitelistCache = null;
    }

    private static class WhitelistCache {

        private final List<String> pathPatterns;

        private final long expireTimeMillis;

        private WhitelistCache(List<String> pathPatterns, long expireTimeMillis) {
            this.pathPatterns = pathPatterns;
            this.expireTimeMillis = expireTimeMillis;
        }
    }
}
