package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.mapper.DictBasicMapper;
import com.erp.server.oms.service.DictBasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
    @Resource
    private RedisService redisService;

    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.OMS_DICT_BASIC_BY_TYPE,
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
            cacheNames = RedisCacheConstants.OMS_DICT_BASIC_BY_TYPE,
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
     * @date 2023-03-17 12:21
     */
    @Override
    @CacheEvict(
            cacheNames = RedisCacheConstants.OMS_DICT_BASIC_BY_TYPE,
            key = "#list[0].type"
    )
    public Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> list) {
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
    @Cacheable(cacheNames = RedisCacheConstants.OMS_DICT_BASIC_BY_TYPE, key = "#key")
    public List<DictBasicEntity> getByKey(String key) {
        return listByKey(key);
    }

    /**
     * 根据key 获取字典数据
     *
     * @param type
     * @param subType
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author hyj
     * @date 2024-05-23 14:16
     */
    @Override
    public List<DictBasicDTO.ViewDTO> getByType(String type, String subType) {
        List<DictBasicEntity> list = listByType(type, subType);
        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class);
        return resultList;
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
            String redisKey = String.format("cache:%s:dict:type::%s", "oms", type);
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
                String redisKey = String.format("cache:%s:dict:type::%s", "oms", type);
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
        queryWrapper.eq(DictBasicEntity::getValue, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }


    private List<DictBasicEntity> listByKey(String key) {
        if (CharSequenceUtil.isBlank(key)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        queryWrapper.eq(DictBasicEntity::getStatus, Boolean.TRUE);
        return this.list(queryWrapper);
    }

    private List<DictBasicEntity> listByType(String type, String subType) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, type);
        if (StringUtils.isNotBlank(subType)) {
            queryWrapper.eq(DictBasicEntity::getSubType, subType);
        }
        queryWrapper.eq(DictBasicEntity::getStatus, Boolean.TRUE);
        return this.list(queryWrapper);
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
    public List<BaseDropDownDTO.Tree> getTreeByKey(String key) {
        List<DictBasicEntity> list = getByKey(key);
        //list 根据sort排序
        list = list.stream().sorted(Comparator.comparingInt(DictBasicEntity::getSort)).collect(Collectors.toList());

        Map<String, List<DictBasicEntity>> map = list.stream().collect(Collectors.groupingBy(DictBasicEntity::getSubType));
        List<BaseDropDownDTO.Tree> treeList = new ArrayList<>();
        map.forEach((subType, viewList) -> {
            BaseDropDownDTO.Tree tree = new BaseDropDownDTO.Tree();
            tree.setCode(subType);
            tree.setValue(DictBasicTypeEnum.getName(subType));
            List<BaseDropDownDTO.ChildTree> childTreeList = new ArrayList<>();
            viewList.forEach(viewDTO -> {
                BaseDropDownDTO.ChildTree childTree = new BaseDropDownDTO.ChildTree();
                childTree.setCode(viewDTO.getValue());
                childTree.setValue(viewDTO.getName());
                childTreeList.add(childTree);
            });
            tree.setChildTreeList(childTreeList);
            treeList.add(tree);
        });
        return treeList;
    }

    @Override
    public List<DictBasicDTO.ViewDTO> listSalesPlatform(String key) {
        List<DictBasicEntity> list = listByKey(key);
        if (DictBasicTypeEnum.SALES_PLATFORM.getType().equals(key)) {
            //销售平台下拉框去除全托管平台类型
            List<DictBasicEntity> dictList = listByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
            if (CollUtil.isNotEmpty(dictList)) {
                list = list.stream().filter(x -> !dictList.stream().map(DictBasicEntity::getValue).collect(Collectors.toList()).contains(x.getValue())).collect(Collectors.toList());
            }
        }
        return BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class);
    }

    @Override
    public List<BaseDropDownDTO.CommonDTO> listInternalSalesPlatform(String key) {
        List<DictBasicEntity> list = lambdaQuery()
                .eq(DictBasicEntity::getType, key)
                .eq(DictBasicEntity::getStatus, Boolean.TRUE)
                .list();

        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class).stream().sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort)).collect(Collectors.toList());
        resultList.sort(Comparator.comparing(DictBasicDTO.ViewDTO::getRemark));
        List<BaseDropDownDTO.CommonDTO> result = resultList.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getValue(), x.getName()))
                .collect(Collectors.toList());


        return result;
    }
}
