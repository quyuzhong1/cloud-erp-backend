package com.erp.server.scm.service.impl;

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
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.server.scm.mapper.DictBasicMapper;
import com.erp.server.scm.service.DictBasicService;

import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
    @Resource
    private RedisService redisService;
	@Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.SCM_DICT_BASIC_BY_TYPE,
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
            cacheNames = RedisCacheConstants.SCM_DICT_BASIC_BY_TYPE,
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

    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 12:21
     */
    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.SCM_DICT_BASIC_BY_TYPE,
            key = "#list[0].type"
    )
    public Boolean saveOrUpdateDict(List<DictBasicDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return this.saveOrUpdateBatch(addList);
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
            cacheNames = RedisCacheConstants.SRM_DICT_BASIC_BY_TYPE,
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
            String redisKey = String.format("cache:%s:dict:type::%s", "scm", type);
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
                String redisKey = String.format("cache:%s:dict:type::%s", "scm", type);
                redisService.setCacheObject(redisKey, list, 8L, TimeUnit.HOURS);
                result.addAll(list);
            }
        }
        return result;
    }

    @Override
    public List<DictBasicEntity> listByNameList(List<String> nameList, DictBasicEnum dictBasicEnum) {
        if (CollUtil.isEmpty(nameList) || dictBasicEnum == null) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(DictBasicEntity::getName,nameList).eq(DictBasicEntity::getType,dictBasicEnum.getType()).list();
    }

    @Override
    public List<DictBasicDTO> tree(String key) {
        List<DictBasicEntity> list = listByKey(key);
        return buildTree(BeanMapperUtils.copyList(DictBasicDTO.class, list));
    }

    /**
     * @return 构建好的树形结构列表
     */
    public List<DictBasicDTO> buildTree(List<DictBasicDTO> treeList) {
        // 获取所有的根节点（没有父级的节点，通常 parentId 为 null 或空）
        List<DictBasicDTO> rootNodes = treeList.stream()
                .filter(item -> ObjectUtils.isEmpty(item.getRemark()))
                .collect(Collectors.toList());
        // 递归设置子节点
        rootNodes.forEach(root -> setChildren(root, treeList));
        return rootNodes;
    }

    /**
     * 递归设置子节点
     *
     * @param parentNode 父节点
     * @param allNodes   所有的节点数据
     */
    private void setChildren(DictBasicDTO parentNode, List<DictBasicDTO> allNodes) {
        // 找到所有 parentId 等于父节点 id 的节点，作为其子节点
        List<DictBasicDTO> children = allNodes.stream()
                .filter(item -> parentNode.getId().equals(item.getRemark()))
                .collect(Collectors.toList());
        // 设置子节点
        parentNode.setChildList(children);
        // 对每个子节点递归查找其子节点
        children.forEach(child -> setChildren(child, allNodes));
    }


    private List<DictBasicEntity> listByKey(String key) {
        if (CharSequenceUtil.isBlank(key)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        return this.list(queryWrapper);
    }

}
