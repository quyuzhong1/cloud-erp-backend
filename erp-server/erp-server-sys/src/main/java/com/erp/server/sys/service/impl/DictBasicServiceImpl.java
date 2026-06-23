package com.erp.server.sys.service.impl;

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
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.mapper.DictBasicMapper;
import com.erp.server.sys.service.DictBasicService;

import javax.annotation.Resource;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {

    @Resource
    private RedisService redisService;

    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.SYS_DICT_BASIC_BY_TYPE,
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
            cacheNames = RedisCacheConstants.SYS_DICT_BASIC_BY_TYPE,
            key = "#jsonObjects[0].getString('type')"
    )
    public boolean updateJsonObject(List<JSONObject> jsonObjects) {
        List<DictBasicEntity> entityList = new ArrayList<>();
        for (JSONObject jsonObject : jsonObjects) {
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

    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 15:16
     */
    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.SYS_DICT_BASIC_BY_TYPE,
            key = "#list[0].type"
    )
    public Boolean addOrUpdate(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<DictBasicEntity> addOrList = BeanMapper.copyList(list, DictBasicEntity.class);
            this.saveOrUpdateBatch(addOrList);
        }
        return Boolean.TRUE;
    }


    /**
     * 根据类型获取字典值
     *
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.DictBasicDTO.ViewDTO>
     * @author yl
     * @date 2023-04-26 15:33
     */
    @Override
    @Cacheable(
            cacheNames = RedisCacheConstants.SYS_DICT_BASIC_BY_TYPE,
            key = "#type"
    )
    public List<DictBasicEntity> listByType(String type) {
        if (CharSequenceUtil.isBlank(type)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(DictBasicEntity::getType, type).list();
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
            String redisKey = String.format("cache:%s:dict:type::%s", "sys", type);
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
                String redisKey = String.format("cache:%s:dict:type::%s", "sys", type);
                redisService.setCacheObject(redisKey, list, 8L, TimeUnit.HOURS);
                result.addAll(list);
            }
        }
        return result;
    }


    /**
     * 根据值获取字典信息
     *
     * @param itemRoleValueList
     * @return java.util.List<com.erp.model.sys.entity.DictBasicEntity>
     * @author yl
     * @date 2023-04-27 14:58
     */
    @Override
    public List<DictBasicEntity> listByValues(List<String> itemRoleValueList) {
        if (CollectionUtils.isEmpty(itemRoleValueList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictBasicEntity::getValue, itemRoleValueList).list();
    }

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return DictBasicEntity
     * @author yl
     * @date 2023-06-28 16:25
     */
    @Override
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, type);
        queryWrapper.eq(DictBasicEntity::getValue, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

}
