package com.erp.server.plm.service.impl;

import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.server.plm.mapper.BasicDictMapper;
import com.erp.server.plm.service.BasicDictService;

import cn.hutool.core.collection.CollUtil;

/**
 * <p>
 * plm 字典表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class BasicDictServiceImpl extends ServiceImpl<BasicDictMapper, BasicDictEntity> implements BasicDictService {

	@Override
	public boolean saveJsonObject(JSONObject jsonObject) {
		BasicDictEntity entity = JSON.parseObject(jsonObject.toJSONString(), BasicDictEntity.class);
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
		List<BasicDictEntity> entityList = new ArrayList<>();
		for(JSONObject jsonObject : jsonObjects) {
			BasicDictEntity entity = JSON.parseObject(jsonObject.toJSONString(), BasicDictEntity.class);
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
     * 保存或者修改plm 字典表
     *
     * @param dtos
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-16 11:38
     */
    @Override
    public Boolean saveOrUpdateDict(List<BasicDictDTO> dtos) {
        List<BasicDictEntity> saveList = BeanMapper.copyList(dtos, BasicDictEntity.class);
        return this.saveOrUpdateBatch(saveList);
    }


    /**
     * 根据属性获取对应的值
     *
     * @param type
     * @return java.util.List<com.erp.model.plm.entity.BasicDictEntity>
     * @author yl
     * @date 2022-09-16 14:27
     */
    @Override
    public List<BasicDictEntity> listByType(String type) {
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicDictEntity::getType, type);
        queryWrapper.orderByDesc(BasicDictEntity::getOrderIndex);
        return this.list(queryWrapper);
    }

    @Override
    public List<BasicDictEntity> listByTypeList(List<String> typeList) {
        if (CollUtil.isEmpty(typeList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BasicDictEntity::getType,typeList).list();
    }

    @Override
    public Map<String, String> mapByType(String type) {
        return  this.list(new LambdaQueryWrapper<BasicDictEntity>().eq(BasicDictEntity::getType, type).orderByDesc(BasicDictEntity::getOrderIndex)).stream().collect(Collectors.toMap(BasicDictEntity::getValue, BasicDictEntity::getName, (v1, v2) -> v1));
    }

    @Override
    public BasicDictEntity listByTypeAndValue(String type, String value) {
        BasicDictEntity entity = lambdaQuery()
                .eq(BasicDictEntity::getType, type)
                .eq(BasicDictEntity::getValue, value)
                .last("limit 1")
                .one();
        return entity;
    }

    /**
     * 根据id集合批量查询字典信息
     *
     * @param list id集合
     * @return java.util.List<com.erp.model.plm.entity.BasicDictEntity>
     * @Author Luo_WG
     * @Date 2022/10/22 19:50
     **/
    @Override
    public List<BasicDictEntity> listByIds(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BasicDictEntity::getId, list);
        return this.list(queryWrapper);
    }

    /**
     * @param type:字典类型
     * @param value:字典值
     * @return com.erp.model.plm.entity.BasicDictEntity
     * @Description 根据名称查询字段是否存在
     * @Author Luo_WG
     * @Date 2022/9/29 11:02
     **/
    public BasicDictEntity checkBasicDict(String type, String value) {
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicDictEntity::getType, type);
        queryWrapper.eq(BasicDictEntity::getValue, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    @Cacheable(cacheNames = "cache:plm:listDictDropDown",keyGenerator = "myKeyGenerator")
    public List<DictControllerDTO.DictDropDownDTO> listDictDropDown(String code) {
        List<BasicDictEntity> list = this.lambdaQuery()
                .eq(isNotBlank(code), BasicDictEntity::getType, code)
                .list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<DictControllerDTO.DictDropDownDTO> result = list.stream()
                .map(DictControllerDTO.DictDropDownDTO::new)
                .collect(Collectors.toList());
        return result;
    }
}
