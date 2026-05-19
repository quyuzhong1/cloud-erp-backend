package com.erp.server.tms.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.server.tms.mapper.DictBasicMapper;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.OperateLogService;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DictBasicServiceImpl dictBasicService;
    @Resource
    private RedisService redisService;
    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.TMS_DICT_BASIC_BY_TYPE,
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
            cacheNames = RedisCacheConstants.TMS_DICT_BASIC_BY_TYPE,
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.TMS_DICT_BASIC_BY_TYPE,
            key = "#list[0].type"
    )
    public Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return dictBasicService.saveOrUpdateBatch(addList);
    }


    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author yl
     * @date 2023-03-17 14:16
     */
    @Override
    @Cacheable(
            cacheNames = RedisCacheConstants.TMS_DICT_BASIC_BY_TYPE,
            key = "#key"
    )
    public List<DictBasicEntity> getByKey(String key) {
        return listByKey(key);
    }


    /**
     * 根据key list 获取对应数据
     *
     * @param typeList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
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
            String redisKey = String.format("cache:%s:dict:type::%s", "tms", type);
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
                String redisKey = String.format("cache:%s:dict:type::%s", "tms", type);
                redisService.setCacheObject(redisKey, list, 8L, TimeUnit.HOURS);
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     * @author yl
     * @date 2023-06-28 16:25
     */
    @Override
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, type);
        queryWrapper.eq(DictBasicEntity::getCode, value);
        queryWrapper.last(SqlConstants.LIMIT_1);
        return this.getOne(queryWrapper);
    }


    private List<DictBasicEntity> listByKey(String key) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        queryWrapper.orderByAsc(DictBasicEntity::getIndex);
        return this.list(queryWrapper);

    }
}
