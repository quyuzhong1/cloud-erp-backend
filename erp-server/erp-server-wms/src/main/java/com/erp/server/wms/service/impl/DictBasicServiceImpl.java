package com.erp.server.wms.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.server.wms.mapper.DictBasicMapper;
import com.erp.server.wms.service.DictBasicService;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
@Slf4j
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {

    /**
     * 字典批量查询单批最大 ID 数量。该接口对外通过 Feign 暴露（{@link com.erp.server.wms.controller.feign.DictBasicFeignController}），
     * 限制单次 IN 查询规模，避免恶意或异常调用造成大 IN 查询打到数据库。
     */
    private static final int LIST_VALUE_MAP_MAX_BATCH_SIZE = 200;

	@Override
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
	
    @Resource
    private RedisService redisService;

    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 12:21
     */
    @Override
    public Boolean saveOrUpdateDict(List<DictBasicDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        Boolean result = this.saveOrUpdateBatch(addList);

        return result;
    }


    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return
     * @author yl
     * @date 2023-03-17 14:16
     */
    @Override
    public List<DictBasicDTO.ListDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO.ListDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ListDTO.class);
        return resultList;
    }


    /**
     * 根据key list 获取对应数据
     *
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     * @author yl
     * @date 2023-03-20 14:24
     */
    @Override
    public List<DictBasicEntity> getByKeyList(List<String> keyList) {
        if (CollectionUtils.isEmpty(keyList)) {
            return listAll();
        }
        List<DictBasicEntity> allList = this.lambdaQuery().in(DictBasicEntity::getType, keyList).list();
        return allList;
    }

    @Override
    public List<DictBasicDTO.DropDownDTO> listByType(String type, String remark) {
        List<DictBasicEntity> list = lambdaQuery().eq(DictBasicEntity::getType, type)
                .eq("processCondition".equalsIgnoreCase(type), DictBasicEntity::getRemark, remark)
                .orderByAsc(DictBasicEntity::getSort)
                .list();
        List<DictBasicDTO.DropDownDTO> result = list.stream().map(DictBasicDTO.DropDownDTO::new).collect(Collectors.toList());
        return result;
    }


    private List<DictBasicEntity> listByKey(String type) {
        if (CharSequenceUtil.isBlank(type)) {
            return Collections.emptyList();
        }
        List<DictBasicEntity> allList = this.lambdaQuery().
                eq(DictBasicEntity::getType,type).
                orderByAsc(DictBasicEntity::getSort).list();
        return  allList;
    }

    /**
     * 获取 所有的
     *
     * @return
     */
    private List<DictBasicEntity> listAll() {

//        String redisKey = RedisCacheConstants.WMS_DICT_KEY;
//        List<DictBasicEntity> dictList = redisService.getCacheList(redisKey);
//        if (CollectionUtils.isNotEmpty(dictList)) {
//            return dictList;
//        }
        List<DictBasicEntity> list = this.list();
//        if (CollectionUtils.isNotEmpty(list)) {
//            redisService.setCacheList(redisKey, list);
//        }
        return list;

    }

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.wms.entity.DictBasicEntity
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

    @Override
    public Map<String, String> listValueMapByTypeAndIds(String type, List<String> ids) {
        if (CharSequenceUtil.isBlank(type) || CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        if (ids.size() > LIST_VALUE_MAP_MAX_BATCH_SIZE) {
            log.warn("listValueMapByTypeAndIds 单次查询数量超过上限, type={}, size={}, max={}",
                    type, ids.size(), LIST_VALUE_MAP_MAX_BATCH_SIZE);
            throw new com.common.core.exception.ServiceException(
                    CharSequenceUtil.format("字典批量查询单次最多支持 {} 条, 当前 {} 条",
                            LIST_VALUE_MAP_MAX_BATCH_SIZE, ids.size()));
        }
        List<String> distinctIds = ids.stream().distinct().collect(Collectors.toList());
        List<DictBasicEntity> list = this.lambdaQuery()
                .eq(DictBasicEntity::getType, type)
                .in(DictBasicEntity::getId, distinctIds)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(list.size());
        for (DictBasicEntity entity : list) {
            result.put(entity.getId(), entity.getValue());
        }
        return result;
    }

}
