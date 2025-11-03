package com.erp.server.dmp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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

/**
 * @author Will
 * @version 1.0
 * @description: 字典表 服务实现类
 * @date 2023/10/16 15:31
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {

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
	
    @Override
    public Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return this.saveOrUpdateBatch(addList);
    }

    @Override
    public List<DictBasicDTO.ViewDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class);
        return resultList;
    }

    private List<DictBasicEntity> listByKey(String key) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        return this.list(queryWrapper);
    }
}
