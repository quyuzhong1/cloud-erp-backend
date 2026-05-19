package com.erp.server.dmp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.server.dmp.mapper.DictBasicMapper;
import com.erp.server.dmp.service.DictBasicService;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: 字典表 服务实现类
 * @date 2023/10/16 15:31
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
	@Resource
	private RedisService redisService;
	@Override
	@CacheEvict(
			cacheNames = RedisCacheConstants.DMP_DICT_BASIC_BY_TYPE,
			key = "#jsonObject.getString('type')"
	)
	public boolean saveJsonObject(JSONObject jsonObject) {
		DictBasicEntity entity = JSON.parseObject(jsonObject.toJSONString(), DictBasicEntity.class);
		LocalDateTime now = LocalDateTime.now();
		LoginUser loginUser = UserContext.getNonLoginUser();
		String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
    	entity.setUpdateTime(now);
        entity.setUpdateUserId(userId);
        entity.setUpdateUserName(userName);
        
        entity.setCreateTime(now);
		entity.setCreateUserId(userId);
		entity.setCreateUserName(userName);
		return super.save(entity);
	}
	
	@Override
	@CacheEvict(
			cacheNames = RedisCacheConstants.DMP_DICT_BASIC_BY_TYPE,
			key = "#jsonObjects[0].getString('type')"
	)
	public boolean updateJsonObject(List<JSONObject> jsonObjects) {
		List<DictBasicEntity> entityList = new ArrayList<>();
		for(JSONObject jsonObject : jsonObjects) {
			DictBasicEntity entity = JSON.parseObject(jsonObject.toJSONString(), DictBasicEntity.class);
			LocalDateTime now = LocalDateTime.now();
			LoginUser loginUser = UserContext.getNonLoginUser();
			String userId = loginUser.getUid();
	        String userName = loginUser.getUserName();
	    	entity.setUpdateTime(now);
	        entity.setUpdateUserId(userId);
	        entity.setUpdateUserName(userName);
	        entityList.add(entity);
		}
        return super.updateBatchById(entityList);
	}
	
    @Override
	@CacheEvict(
			cacheNames = RedisCacheConstants.DMP_DICT_BASIC_BY_TYPE,
			key = "#list[0].type"
	)
    public Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return this.saveOrUpdateBatch(addList);
    }

    @Override
	@Cacheable(cacheNames = RedisCacheConstants.DMP_DICT_BASIC_BY_TYPE, key = "#key")
    public List<DictBasicEntity> getByKey(String key) {
		return listByKey(key);
    }

	/**
	 * 根据key list 获取对应数据
	 *
	 * @param typeList
	 * @return java.util.List<DictBasicEntity>
	 * @author yl
	 * @date 2023-03-20 14:24
	 */
	@Override
	public List<DictBasicEntity> getByKeyList(List<String> typeList) {
		if (CollectionUtils.isEmpty(typeList)) {
			return new ArrayList<>();
		}
		List<String> types = typeList.stream()
				.filter(StringUtils::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
		List<DictBasicEntity> result = new ArrayList<>();
		List<String> missTypes = new ArrayList<>();
		for (String type : types) {
			String redisKey = String.format("cache:%s:dict:type::%s", "dmp", type);
			List<DictBasicEntity> cacheList = redisService.getCacheObject(redisKey);
			if (cacheList != null) {
				result.addAll(cacheList);
			} else {
				missTypes.add(type);
			}
		}
		if (CollectionUtils.isNotEmpty(missTypes)) {
			List<DictBasicEntity> dbList = this.lambdaQuery()
					.in(DictBasicEntity::getType, missTypes)
					.list();
			Map<String, List<DictBasicEntity>> dbMap = dbList.stream()
					.collect(Collectors.groupingBy(DictBasicEntity::getType));
			for (String type : missTypes) {
				List<DictBasicEntity> list = dbMap.getOrDefault(type, new ArrayList<>());
				String redisKey = String.format("cache:%s:dict:type::%s", "dmp", type);
				redisService.setCacheObject(redisKey, list, 8L, TimeUnit.HOURS);
				result.addAll(list);
			}
		}
		return result;
	}

    private List<DictBasicEntity> listByKey(String key) {
		if (CharSequenceUtil.isBlank(key)) {
			return Collections.emptyList();
		}
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        return this.list(queryWrapper);
    }

    @Override
    public List<DictBasicEntity> getByType(String type) {
        return lambdaQuery()
                .eq(StringUtils.isNotBlank(type), DictBasicEntity::getType, type)
                .list();
    }
}
