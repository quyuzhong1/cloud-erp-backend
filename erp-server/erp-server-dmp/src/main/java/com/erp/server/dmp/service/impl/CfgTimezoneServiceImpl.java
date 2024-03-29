package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.sellers.Marketplace;
import com.erp.server.dmp.mapper.CfgTimezoneMapper;
import com.erp.server.dmp.service.CfgTimezoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
        List<String> keyList = Arrays.stream(AmazonMarketplaceEnum.values())
                .map(e -> StrUtil.format(RedisCacheConstants.CFG_TIMEZONE_PREFIX, e.getCountryCode()))
                .collect(Collectors.toList());

        // 缓存获取
        List<CfgTimezoneEntity> cacheList = redisTemplate.opsForValue().multiGet(keyList);
        if (null != cacheList){
            cacheList = cacheList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(cacheList)){
                return cacheList;
            }
        }
        List<CfgTimezoneEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        Map<String, CfgTimezoneEntity> map = list.stream()
                .collect(Collectors.toMap(entity -> StrUtil.format(RedisCacheConstants.CFG_TIMEZONE_PREFIX, entity.getCountry()), Function.identity()));
        redisTemplate.opsForValue().multiSet(map);
        return list;
    }

    @Override
    public CfgTimezoneEntity getAndCacheByCountry(String country) {
        return mapByCountry().get(country);
    }
}
