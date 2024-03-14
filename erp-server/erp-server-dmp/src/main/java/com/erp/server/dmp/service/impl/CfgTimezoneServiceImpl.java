package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.server.dmp.mapper.CfgTimezoneMapper;
import com.erp.server.dmp.service.CfgTimezoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 国家对应的时区配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-13
 */
@Slf4j
@Service
public class CfgTimezoneServiceImpl extends SuperServiceImpl<CfgTimezoneMapper, CfgTimezoneEntity> implements CfgTimezoneService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisUtil redisUtil;


    @Override
    public Map<String, CfgTimezoneEntity> mapByCountry() {
        List<CfgTimezoneEntity> list = listAndCache();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(CfgTimezoneEntity::getCountry, Function.identity()));
    }

    @Override
    public List<CfgTimezoneEntity> listAndCache() {
        String listKey = RedisCacheConstants.CFG_TIMEZONE;
        // 缓存获取
        List<CfgTimezoneEntity> cacheList = redisTemplate.opsForList().range(listKey, 0, -1);
        if (!CollectionUtils.isEmpty(cacheList)){
            return cacheList;
        }
        List<CfgTimezoneEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        for (CfgTimezoneEntity entity : list) {
            String currentKey = StrUtil.format(RedisCacheConstants.CFG_TIMEZONE_PREFIX, entity.getCountry());
            redisTemplate.opsForList().rightPush(currentKey, entity);
        }
        return list;
    }

    @Override
    public CfgTimezoneEntity getAndCacheByCountry(String country) {
        return mapByCountry().get(country);
    }
}
